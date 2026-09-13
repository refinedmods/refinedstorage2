package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.impl.node.NetworkNodeDetailsChangedEvent;
import com.refinedmods.refinedstorage.api.network.impl.node.StorageContentsChangedEvent;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorListener;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeId;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeTypeId;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepository;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceCategory;
import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceType;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;
import com.refinedmods.refinedstorage.common.support.stretching.ScreenSizeListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class NetworkMonitorContainerMenu extends AbstractBaseContainerMenu implements ScreenSizeListener,
    MonitorListener {
    private final RateLimiter networkStatisticsUpdateRateLimiter = RateLimiter.create(2);
    private final Predicate<Player> stillValid;
    private final NetworkMonitorDevices devices;
    @Nullable
    private NetworkMonitorListener listener;
    @Nullable
    private NetworkMonitorBlockEntity networkMonitor;
    @Nullable
    private Player player;
    private NetworkMonitorNetworkStatistics lastNetworkStatistics;
    private boolean active;
    private NetworkMonitorGroupType groupType = Platform.INSTANCE.getConfig().getNetworkMonitor().getGroupType();
    @Nullable
    private NetworkMonitorDeviceCategory viewType = Platform.INSTANCE.getConfig().getNetworkMonitor().getViewType()
        .orElse(null);
    private NetworkMonitorSortingType sortingType = Platform.INSTANCE.getConfig().getNetworkMonitor().getSortingType();
    private NetworkMonitorSortingDirection sortingDirection = Platform.INSTANCE.getConfig().getNetworkMonitor()
        .getSortingDirection();
    private Comparator<NetworkMonitorDeviceGroup> deviceGroupSorter;
    private Comparator<NetworkMonitorDeviceCategory> deviceCategorySorter;
    private Comparator<NetworkMonitorDevice> deviceSorter;
    private boolean emptyDeviceCategoryWarningVisible;
    private final NetworkMonitorClientSelection clientSelection = new NetworkMonitorClientSelection();
    @Nullable
    private NetworkMonitorServerSelection serverSelection;

    public NetworkMonitorContainerMenu(final int syncId, final NetworkMonitorData data) {
        super(Menus.INSTANCE.getNetworkMonitor(), syncId);
        this.stillValid = p -> true;
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        this.devices = new NetworkMonitorDevices(data.deviceGroups());
        updateEmptyDeviceCategoryWarning();
        this.active = data.active();
        this.lastNetworkStatistics = data.networkStatistics();
        updateSorters();
    }

    public NetworkMonitorContainerMenu(final int syncId,
                                       final Inventory playerInventory,
                                       final NetworkMonitorBlockEntity networkMonitor) {
        super(Menus.INSTANCE.getNetworkMonitor(), syncId);
        this.stillValid = p -> Container.stillValidBlockEntity(networkMonitor, p);
        this.networkMonitor = networkMonitor;
        this.devices = new NetworkMonitorDevices(Collections.emptyList());
        this.player = playerInventory.player;
        this.lastNetworkStatistics = networkMonitor.getNetworkStatistics();
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            networkMonitor::getRedstoneMode,
            networkMonitor::setRedstoneMode
        ));
        networkMonitor.addListener(this);
        updateSorters();
        this.serverSelection = new NetworkMonitorServerSelection(networkMonitor, this::sendDetails,
            this::handleEvent);
    }

    @Override
    public void removed(final Player playerEntity) {
        super.removed(playerEntity);
        if (networkMonitor != null) {
            networkMonitor.removeListener(this);
        }
        if (serverSelection != null) {
            serverSelection.removed();
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (serverSelection != null) {
            serverSelection.tick();
        }
        if (networkMonitor != null
            && player instanceof ServerPlayer serverPlayer
            && networkStatisticsUpdateRateLimiter.tryAcquire()) {
            final NetworkMonitorNetworkStatistics oldNetworkStatistics = lastNetworkStatistics;
            lastNetworkStatistics = networkMonitor.getNetworkStatistics();
            if (!lastNetworkStatistics.equals(oldNetworkStatistics)) {
                S2CPackets.sendNetworkMonitorNetworkStatisticsUpdate(serverPlayer, lastNetworkStatistics);
            }
        }
    }

    List<NetworkMonitorDeviceGroup> getDeviceGroups() {
        return devices.getAll();
    }

    void onSearchTextChanged(final String text) {
        devices.onSearchTextChanged(text);
    }

    boolean isVisible(final NetworkMonitorDeviceGroup deviceGroup) {
        return devices.isVisible(deviceGroup);
    }

    boolean isVisible(final NetworkMonitorDeviceCategory deviceCategory) {
        return devices.isVisible(deviceCategory);
    }

    boolean isVisible(final NetworkMonitorDevice device) {
        return devices.isVisible(device);
    }

    NetworkMonitorGroupType getGroupType() {
        return groupType;
    }

    @Nullable
    NetworkMonitorDeviceCategory getViewType() {
        return viewType;
    }

    void setGroupType(final NetworkMonitorGroupType groupType) {
        Platform.INSTANCE.getConfig().getNetworkMonitor().setGroupType(groupType);
        this.groupType = groupType;
        if (listener != null) {
            listener.onGroupTypeChanged(groupType);
        }
    }

    void setViewType(@Nullable final NetworkMonitorDeviceCategory viewType) {
        if (viewType == null) {
            Platform.INSTANCE.getConfig().getNetworkMonitor().clearViewType();
        } else {
            Platform.INSTANCE.getConfig().getNetworkMonitor().setViewType(viewType);
        }
        this.viewType = viewType;
        devices.onViewTypeChanged();
        if (listener != null) {
            listener.onViewTypeChanged(viewType);
        }
        updateEmptyDeviceCategoryWarning();
    }

    NetworkMonitorSortingType getSortingType() {
        return sortingType;
    }

    void setSortingType(final NetworkMonitorSortingType sortingType) {
        Platform.INSTANCE.getConfig().getNetworkMonitor().setSortingType(sortingType);
        this.sortingType = sortingType;
        updateSorters();
        if (listener != null) {
            listener.onSortingTypeChanged(sortingType);
        }
    }

    NetworkMonitorSortingDirection getSortingDirection() {
        return sortingDirection;
    }

    void setSortingDirection(final NetworkMonitorSortingDirection sortingDirection) {
        Platform.INSTANCE.getConfig().getNetworkMonitor().setSortingDirection(sortingDirection);
        this.sortingDirection = sortingDirection;
        updateSorters();
        if (listener != null) {
            listener.onSortingDirectionChanged(sortingDirection);
        }
    }

    private void updateSorters() {
        this.deviceGroupSorter = sortingType.getDeviceGroupComparator()
            .thenComparing(NetworkMonitorSortingType.NAME.getDeviceGroupComparator());
        this.deviceCategorySorter = sortingType.getDeviceCategoryComparator().apply(devices)
            .thenComparing(NetworkMonitorSortingType.NAME.getDeviceCategoryComparator().apply(devices));
        this.deviceSorter = sortingType.getDeviceComparator()
            .thenComparing(NetworkMonitorSortingType.NAME.getDeviceComparator());
        if (sortingDirection == NetworkMonitorSortingDirection.DESCENDING) {
            this.deviceGroupSorter = deviceGroupSorter.reversed();
            this.deviceCategorySorter = deviceCategorySorter.reversed();
            this.deviceSorter = deviceSorter.reversed();
        }
    }

    Comparator<NetworkMonitorDeviceGroup> getDeviceGroupSorter() {
        return deviceGroupSorter;
    }

    Comparator<NetworkMonitorDeviceCategory> getDeviceCategorySorter() {
        return deviceCategorySorter;
    }

    Comparator<NetworkMonitorDevice> getDeviceSorter() {
        return deviceSorter;
    }

    boolean isSearching() {
        return devices.isSearching();
    }

    @Nullable
    NetworkMonitorDeviceGroup getCurrentDeviceGroup() {
        return clientSelection.getDeviceGroup();
    }

    @Nullable
    NetworkMonitorDeviceCategory getCurrentDeviceCategory() {
        return clientSelection.getDeviceCategory();
    }

    @Nullable
    NetworkMonitorDevice getCurrentDevice() {
        return clientSelection.getDevice();
    }

    @Nullable
    NetworkNodeDetails getCurrentDetails() {
        return clientSelection.getDetails();
    }

    @Nullable
    public ResourceRepository<GridResource> getDetailsRepository() {
        return clientSelection.getDetailsRepository();
    }

    public void updateServerSelection(@Nullable final MonitorNodeId deviceId,
                                      @Nullable final MonitorNodeTypeId deviceGroupId,
                                      @Nullable final NetworkMonitorDeviceCategory deviceCategory) {
        if (serverSelection == null) {
            return;
        }
        serverSelection.setSelectedDevice(deviceId, deviceGroupId, deviceCategory);
    }

    void setCurrentDeviceGroup(@Nullable final NetworkMonitorDeviceGroup deviceGroup) {
        clientSelection.update(deviceGroup, null, null);
        if (listener != null) {
            listener.onCurrentDeviceGroupChanged(deviceGroup);
        }
        notifyDetailsChanged();
    }

    void setCurrentDeviceCategory(@Nullable final NetworkMonitorDeviceCategory deviceCategory) {
        clientSelection.update(null, deviceCategory, null);
        if (listener != null) {
            listener.onCurrentDeviceCategoryChanged(deviceCategory);
        }
        notifyDetailsChanged();
    }

    void setCurrentDevice(@Nullable final NetworkMonitorDeviceGroup deviceGroup,
                          @Nullable final NetworkMonitorDeviceCategory deviceCategory,
                          @Nullable final NetworkMonitorDevice device) {
        clientSelection.update(deviceGroup, deviceCategory, device);
        if (listener != null) {
            listener.onCurrentDeviceChanged(device);
        }
        notifyDetailsChanged();
    }

    private void notifyDetailsChanged() {
        if (listener != null) {
            listener.onDetailsChanged(clientSelection.getDeviceGroup() == null
                    && clientSelection.getDeviceCategory() == null && clientSelection.getDevice() == null,
                clientSelection.getDetails());
        }
    }

    private void sendDetails(@Nullable final MonitorNodeId id, final NetworkNodeDetails details) {
        if (player instanceof ServerPlayer serverPlayer) {
            S2CPackets.sendNetworkMonitorDetailsUpdate(serverPlayer, id, details);
        }
    }

    private void handleEvent(final MonitorNodeId deviceId, final Object event) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        switch (event) {
            case StorageContentsChangedEvent(ResourceKey resource, long change, long stored, long capacity) -> {
                if (resource instanceof PlatformResourceKey platformResource) {
                    S2CPackets.sendNetworkMonitorDetailsResourceUpdate(serverPlayer, platformResource, change, stored,
                        capacity);
                }
            }
            case NetworkNodeDetailsChangedEvent(long energyUsage, boolean newActive) ->
                S2CPackets.sendNetworkMonitorDeviceUpdated(serverPlayer, deviceId, energyUsage, newActive);
            default -> {
                // no other events are of interest
            }
        }
    }

    public void updateDevice(final MonitorNodeId deviceId, final long energyUsage, final boolean newActive) {
        clientSelection.updateDetails(deviceId, energyUsage, newActive);
        updateDeviceEnergyUsage(deviceId, energyUsage);
    }

    public void updateDetails(@Nullable final MonitorNodeId deviceId, final NetworkNodeDetails details) {
        final var result = clientSelection.updateDetails(deviceId, details);
        if (result == NetworkMonitorClientSelection.DetailsUpdateResult.IGNORED) {
            return;
        }
        // A device is only listened to while it is open, so it can be outdated by the time that it gets opened.
        if (deviceId != null && details instanceof AbstractNetworkNodeDetails baseDetails) {
            updateDeviceEnergyUsage(deviceId, baseDetails.getEnergyUsage());
        }
        if (result == NetworkMonitorClientSelection.DetailsUpdateResult.REFRESHED) {
            if (listener != null) {
                listener.onDetailsRefreshed(details);
            }
            return;
        }
        notifyDetailsChanged();
    }

    private void updateDeviceEnergyUsage(final MonitorNodeId deviceId, final long energyUsage) {
        final NetworkMonitorDevice updatedDevice = devices.updateEnergyUsage(deviceId, energyUsage);
        if (updatedDevice != null && listener != null) {
            listener.onDeviceUpdated(updatedDevice);
        }
    }

    public void updateResource(final ResourceKey resource, final long change,
                               final long stored, final long capacity) {
        clientSelection.updateResource(resource, change, stored, capacity);
    }

    void setListener(final NetworkMonitorListener listener) {
        this.listener = listener;
        devices.setListener(listener);
    }

    boolean isActive() {
        return active;
    }

    @Override
    public boolean stillValid(final Player p) {
        return this.stillValid.test(p);
    }

    @Override
    public void resized(final int playerInventoryY, final int topYStart, final int topYEnd) {
        // no op
    }

    @Override
    public void onNodeTracked(final MonitorNodeId id, final MonitorNodeTypeId typeId) {
        if (!(player instanceof ServerPlayer serverPlayer) || networkMonitor == null) {
            return;
        }
        if (serverSelection != null) {
            serverSelection.onNodeTracked(id, typeId);
        }
        final NetworkMonitorDevice device = networkMonitor.getDevice(id);
        if (device == null) {
            return;
        }
        final NetworkMonitorDeviceType type = networkMonitor.getDeviceType(typeId);
        if (type == null) {
            return;
        }
        S2CPackets.sendNetworkMonitorDeviceAdded(serverPlayer, typeId, type, device);
    }

    public void addDevice(final MonitorNodeTypeId groupId, final NetworkMonitorDeviceType type,
                          final NetworkMonitorDevice device) {
        devices.addDevice(groupId, type, device);
        updateEmptyDeviceCategoryWarning();
    }

    @Override
    public void onNodeUntracked(final MonitorNodeId id) {
        if (serverSelection != null) {
            serverSelection.onNodeUntracked(id);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            S2CPackets.sendNetworkMonitorDeviceRemoved(serverPlayer, id);
        }
    }

    public void removeDevice(final MonitorNodeId id) {
        final boolean removed = clientSelection.removeDevice(id);
        if (removed) {
            if (listener != null) {
                listener.onCurrentDeviceChanged(null);
            }
            notifyDetailsChanged();
        }
        final NetworkMonitorDevices.DeviceRemovalSideEffects sideEffects = devices.removeDevice(id);
        processDeviceRemovalSideEffects(sideEffects);
        updateEmptyDeviceCategoryWarning();
    }

    private void processDeviceRemovalSideEffects(final NetworkMonitorDevices.DeviceRemovalSideEffects sideEffects) {
        if (sideEffects.removedDeviceGroup() != null) {
            final boolean removed = clientSelection.removeDeviceGroup(
                new MonitorNodeTypeId(sideEffects.removedDeviceGroup().id()));
            if (removed && listener != null) {
                listener.onCurrentDeviceGroupChanged(null);
            }
        }
        if (sideEffects.removedDeviceCategory() != null) {
            final boolean removed = clientSelection.removeDeviceCategory(sideEffects.removedDeviceCategory());
            if (removed && listener != null) {
                listener.onCurrentDeviceCategoryChanged(null);
            }
        }
    }

    @Override
    public void onActiveChanged(final boolean newActive) {
        if (player instanceof ServerPlayer serverPlayer) {
            S2CPackets.sendNetworkMonitorActive(serverPlayer, newActive);
            return;
        }
        this.active = newActive;
        if (!active) {
            clientSelection.update(null, null, null);
        }
        if (listener != null) {
            listener.onCurrentDeviceChanged(clientSelection.getDevice());
            notifyDetailsChanged();
            listener.onCurrentDeviceGroupChanged(clientSelection.getDeviceGroup());
            listener.onCurrentDeviceCategoryChanged(clientSelection.getDeviceCategory());
            listener.onActiveChanged(newActive);
        }
    }

    public void onNetworkStatisticsUpdated(final NetworkMonitorNetworkStatistics networkStatistics) {
        this.lastNetworkStatistics = networkStatistics;
    }

    NetworkMonitorNetworkStatistics getLastNetworkStatistics() {
        return lastNetworkStatistics;
    }

    boolean isEmptyDeviceCategoryWarningVisible() {
        return emptyDeviceCategoryWarningVisible;
    }

    private void updateEmptyDeviceCategoryWarning() {
        if (viewType == null) {
            emptyDeviceCategoryWarningVisible = false;
        } else {
            emptyDeviceCategoryWarningVisible = !devices.hasDevices(viewType);
        }
    }
}
