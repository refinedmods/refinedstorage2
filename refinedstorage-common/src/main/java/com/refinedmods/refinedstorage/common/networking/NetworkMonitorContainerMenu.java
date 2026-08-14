package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorListener;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeId;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeTypeId;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceType;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;
import com.refinedmods.refinedstorage.common.support.stretching.ScreenSizeListener;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class NetworkMonitorContainerMenu extends AbstractBaseContainerMenu implements ScreenSizeListener,
    MonitorListener {
    private final Predicate<Player> stillValid;
    private final NetworkMonitorDeviceGroups deviceGroups;
    @Nullable
    private NetworkMonitorDeviceGroup currentDeviceGroup;
    @Nullable
    private NetworkMonitorDevice currentDevice;
    @Nullable
    private NetworkNodeDetails currentDetails;
    @Nullable
    private NetworkMonitorListener listener;
    @Nullable
    private NetworkMonitorBlockEntity networkMonitor;
    @Nullable
    private Player player;

    public NetworkMonitorContainerMenu(final int syncId, final NetworkMonitorData data) {
        super(Menus.INSTANCE.getNetworkMonitor(), syncId);
        this.stillValid = p -> true;
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        this.deviceGroups = new NetworkMonitorDeviceGroups(data.deviceGroups());
    }

    public NetworkMonitorContainerMenu(final int syncId,
                                       final Inventory playerInventory,
                                       final NetworkMonitorBlockEntity networkMonitor) {
        super(Menus.INSTANCE.getNetworkMonitor(), syncId);
        this.stillValid = p -> Container.stillValidBlockEntity(networkMonitor, p);
        this.networkMonitor = networkMonitor;
        this.deviceGroups = new NetworkMonitorDeviceGroups(Collections.emptyList());
        this.player = playerInventory.player;
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            networkMonitor::getRedstoneMode,
            networkMonitor::setRedstoneMode
        ));
        networkMonitor.addListener(this);
    }

    @Override
    public void removed(final Player playerEntity) {
        super.removed(playerEntity);
        if (networkMonitor != null) {
            networkMonitor.removeListener(this);
        }
    }

    List<NetworkMonitorDeviceGroup> getDeviceGroups() {
        return deviceGroups.getAll();
    }

    void onSearchTextChanged(final String text) {
        deviceGroups.onSearchTextChanged(text);
    }

    boolean isVisible(final NetworkMonitorDeviceGroup deviceGroup) {
        return deviceGroups.isVisible(deviceGroup);
    }

    boolean isVisible(final NetworkMonitorDevice device) {
        return deviceGroups.isVisible(device);
    }

    @Nullable
    NetworkMonitorDeviceGroup getCurrentDeviceGroup() {
        return currentDeviceGroup;
    }

    @Nullable
    NetworkMonitorDevice getCurrentDevice() {
        return currentDevice;
    }

    @Nullable
    NetworkNodeDetails getCurrentDetails() {
        return currentDetails;
    }

    void setCurrentDeviceGroup(final NetworkMonitorDeviceGroup deviceGroup) {
        this.currentDeviceGroup = deviceGroup;
        this.currentDevice = null;
        this.currentDetails = null;
        if (listener != null) {
            listener.onCurrentDeviceGroupChanged(deviceGroup);
            listener.onDetailsChanged(null);
        }
    }

    void setCurrentDevice(final NetworkMonitorDeviceGroup deviceGroup, final NetworkMonitorDevice device) {
        this.currentDeviceGroup = deviceGroup;
        this.currentDevice = device;
        this.currentDetails = null;
        if (listener != null) {
            listener.onCurrentDeviceChanged(deviceGroup, device);
            listener.onDetailsChanged(null);
        }
    }

    void setListener(final NetworkMonitorListener listener) {
        this.listener = listener;
        deviceGroups.setListener(listener);
    }

    boolean isActive() {
        return true;
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
        deviceGroups.addDevice(groupId, type, device);
    }

    @Override
    public void onNodeUntracked(final MonitorNodeId id) {
        if (player instanceof ServerPlayer serverPlayer) {
            S2CPackets.sendNetworkMonitorDeviceRemoved(serverPlayer, id);
        }
    }

    public void removeDevice(final MonitorNodeId id) {
        if (currentDevice != null && currentDevice.id().equals(id.id())) {
            currentDevice = null;
            currentDetails = null;
            if (listener != null) {
                listener.onDetailsChanged(null);
            }
        }
        final NetworkMonitorDeviceGroup deviceGroup = deviceGroups.removeDevice(id);
        if (deviceGroup == null) {
            return;
        }
        if (currentDeviceGroup != null && currentDeviceGroup.id().equals(deviceGroup.id())) {
            currentDeviceGroup = null;
        }
    }
}
