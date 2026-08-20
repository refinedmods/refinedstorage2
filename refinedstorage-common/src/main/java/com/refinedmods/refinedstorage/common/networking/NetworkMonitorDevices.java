package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeId;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeTypeId;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceCategory;
import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class NetworkMonitorDevices {
    private final List<NetworkMonitorDeviceGroup> deviceGroups;
    private final List<NetworkMonitorDeviceGroup> deviceGroupsView;
    private final Map<NetworkMonitorDeviceCategory, Set<NetworkMonitorDevice>> devicesByCategory = new HashMap<>();
    private final Set<NetworkMonitorDeviceGroup> visibleDeviceGroups = new HashSet<>();
    private final Set<NetworkMonitorDeviceCategory> visibleDeviceCategories = new HashSet<>();
    private final Set<NetworkMonitorDevice> visibleDevices = new HashSet<>();
    private final Map<NetworkMonitorDeviceCategory, String> deviceCategoryTranslations
        = new EnumMap<>(NetworkMonitorDeviceCategory.class);
    private Predicate<NetworkMonitorDeviceGroup> deviceGroupFilter = createDeviceGroupBaseFilter();
    private Predicate<NetworkMonitorDeviceCategory> deviceCategoryFilter = createDeviceCategoryBaseFilter();
    private BiPredicate<NetworkMonitorDeviceCategory, NetworkMonitorDevice> deviceFilter = createDeviceBaseFilter();
    private boolean searching;
    @Nullable
    private NetworkMonitorListener listener;

    NetworkMonitorDevices(final List<NetworkMonitorDeviceGroup> deviceGroups) {
        this.deviceGroups = deviceGroups;
        this.deviceGroupsView = Collections.unmodifiableList(deviceGroups);
        for (final NetworkMonitorDeviceCategory deviceCategory : NetworkMonitorDeviceCategory.values()) {
            deviceCategoryTranslations.put(deviceCategory, createDeviceCategoryTranslation(deviceCategory).getString());
        }
        for (final NetworkMonitorDeviceGroup deviceGroup : deviceGroups) {
            final NetworkMonitorDeviceCategory deviceCategory = RefinedStorageApi.INSTANCE
                .getNetworkMonitorDeviceCategory(deviceGroup.type());
            loadDeviceGroupIntoCategorizedIndex(deviceGroup, deviceCategory);
            updateDeviceVisibility(deviceGroup, deviceCategory);
        }
    }

    static MutableComponent createDeviceCategoryTranslation(final NetworkMonitorDeviceCategory deviceCategory) {
        return createTranslation("gui", "network_monitor.device_category."
            + deviceCategory.name().toLowerCase(Locale.ROOT));
    }

    private static Predicate<NetworkMonitorDeviceGroup> createDeviceGroupBaseFilter() {
        return group -> Platform.INSTANCE.getConfig().getNetworkMonitor().getViewType()
            .map(deviceCategory ->
                RefinedStorageApi.INSTANCE.getNetworkMonitorDeviceCategory(group.type()) == deviceCategory)
            .orElse(true);
    }

    private static Predicate<NetworkMonitorDeviceCategory> createDeviceCategoryBaseFilter() {
        return deviceCategory -> Platform.INSTANCE.getConfig().getNetworkMonitor().getViewType()
            .map(otherDeviceCategory -> deviceCategory == otherDeviceCategory)
            .orElse(true);
    }

    private static BiPredicate<NetworkMonitorDeviceCategory, NetworkMonitorDevice> createDeviceBaseFilter() {
        return (deviceCategory, device) -> Platform.INSTANCE.getConfig().getNetworkMonitor().getViewType()
            .map(otherDeviceCategory -> deviceCategory == otherDeviceCategory)
            .orElse(true);
    }

    private void loadDeviceGroupIntoCategorizedIndex(final NetworkMonitorDeviceGroup deviceGroup,
                                                     final NetworkMonitorDeviceCategory deviceCategory) {
        devicesByCategory.computeIfAbsent(deviceCategory, k -> new HashSet<>()).addAll(deviceGroup.devices());
    }

    private void updateDeviceVisibility() {
        visibleDevices.clear();
        visibleDeviceGroups.clear();
        visibleDeviceCategories.clear();
        for (final NetworkMonitorDeviceGroup deviceGroup : deviceGroups) {
            final NetworkMonitorDeviceCategory deviceCategory = RefinedStorageApi.INSTANCE
                .getNetworkMonitorDeviceCategory(deviceGroup.type());
            updateDeviceVisibility(deviceGroup, deviceCategory);
        }
    }

    private void updateDeviceVisibility(final NetworkMonitorDeviceGroup deviceGroup,
                                        final NetworkMonitorDeviceCategory deviceCategory) {
        boolean anyDeviceVisible = false;
        for (final NetworkMonitorDevice device : deviceGroup.devices()) {
            anyDeviceVisible |= updateDeviceVisibility(deviceCategory, device);
        }
        if (anyDeviceVisible) {
            visibleDeviceGroups.add(deviceGroup);
            visibleDeviceCategories.add(deviceCategory);
        } else {
            if (deviceGroupFilter.test(deviceGroup)) {
                visibleDeviceGroups.add(deviceGroup);
            }
            if (deviceCategoryFilter.test(deviceCategory)) {
                visibleDeviceCategories.add(deviceCategory);
            }
        }
    }

    private boolean updateDeviceVisibility(final NetworkMonitorDeviceCategory deviceCategory,
                                           final NetworkMonitorDevice device) {
        final boolean visible = deviceFilter.test(deviceCategory, device);
        if (visible) {
            visibleDevices.add(device);
        }
        return visible;
    }

    void setListener(final NetworkMonitorListener listener) {
        this.listener = listener;
    }

    List<NetworkMonitorDeviceGroup> getAll() {
        return deviceGroupsView;
    }

    boolean hasDevices(final NetworkMonitorDeviceCategory deviceCategory) {
        final Set<NetworkMonitorDevice> devicesInCategory = devicesByCategory.get(deviceCategory);
        return devicesInCategory != null && !devicesInCategory.isEmpty();
    }

    long getTotalEnergyUsage(final NetworkMonitorDeviceCategory deviceCategory) {
        final Set<NetworkMonitorDevice> devices = devicesByCategory.get(deviceCategory);
        if (devices == null) {
            return 0;
        }
        return devices.stream().mapToLong(NetworkMonitorDevice::energyUsage).sum();
    }

    @Nullable
    private NetworkMonitorDeviceGroup findDeviceGroupById(final MonitorNodeTypeId groupId) {
        for (final NetworkMonitorDeviceGroup deviceGroup : deviceGroups) {
            if (deviceGroup.id().equals(groupId.id())) {
                return deviceGroup;
            }
        }
        return null;
    }

    @Nullable
    private NetworkMonitorDeviceGroup findDeviceGroupContainingDeviceId(final MonitorNodeId id) {
        return deviceGroups.stream()
            .filter(deviceGroup -> deviceGroup.hasDevice(id))
            .findFirst()
            .orElse(null);
    }

    DeviceRemovalSideEffects removeDevice(final MonitorNodeId id) {
        final NetworkMonitorDeviceGroup deviceGroup = findDeviceGroupContainingDeviceId(id);
        if (deviceGroup == null) {
            return DeviceRemovalSideEffects.NONE;
        }
        final NetworkMonitorDeviceCategory deviceCategory = RefinedStorageApi.INSTANCE
            .getNetworkMonitorDeviceCategory(deviceGroup.type());
        final Set<NetworkMonitorDevice> devicesInCategory = devicesByCategory.get(deviceCategory);
        if (devicesInCategory == null) {
            return DeviceRemovalSideEffects.NONE;
        }
        final NetworkMonitorDevice device = deviceGroup.removeDeviceById(id);
        if (device == null) {
            return DeviceRemovalSideEffects.NONE;
        }
        devicesInCategory.remove(device);
        visibleDevices.remove(device);
        if (listener != null) {
            listener.onDeviceRemoved(deviceGroup, deviceCategory, device);
        }
        final boolean deviceGroupEmpty = deviceGroup.devices().isEmpty();
        final boolean deviceCategoryEmpty = devicesInCategory.isEmpty();
        if (deviceGroupEmpty) {
            removeDeviceGroup(deviceGroup);
        }
        if (deviceCategoryEmpty) {
            removeDeviceCategory(deviceCategory);
        }
        return new DeviceRemovalSideEffects(
            deviceGroupEmpty ? deviceGroup : null,
            deviceCategoryEmpty ? deviceCategory : null
        );
    }

    private void removeDeviceGroup(final NetworkMonitorDeviceGroup deviceGroup) {
        visibleDeviceGroups.remove(deviceGroup);
        deviceGroups.remove(deviceGroup);
        if (listener != null) {
            listener.onDeviceGroupRemoved(deviceGroup);
        }
    }

    private void removeDeviceCategory(final NetworkMonitorDeviceCategory deviceCategory) {
        visibleDeviceCategories.remove(deviceCategory);
        devicesByCategory.remove(deviceCategory);
        if (listener != null) {
            listener.onDeviceCategoryRemoved(deviceCategory);
        }
    }

    void addDevice(final MonitorNodeTypeId groupId,
                   final NetworkMonitorDeviceType type,
                   final NetworkMonitorDevice device) {
        final NetworkMonitorDeviceCategory deviceCategory = RefinedStorageApi.INSTANCE
            .getNetworkMonitorDeviceCategory(type);
        final boolean visible = deviceFilter.test(deviceCategory, device);
        final NetworkMonitorDeviceGroup deviceGroup = findOrAddDeviceGroupById(groupId, type, device, visible);
        deviceGroup.devices().add(device);
        addDeviceToCategoryAndCreateCategoryIfItDoesNotExist(device, deviceCategory, visible);
        if (visible) {
            visibleDevices.add(device);
        }
        if (listener != null) {
            listener.onDeviceAdded(deviceGroup, deviceCategory, device);
        }
    }

    private NetworkMonitorDeviceGroup findOrAddDeviceGroupById(final MonitorNodeTypeId groupId,
                                                               final NetworkMonitorDeviceType type,
                                                               final NetworkMonitorDevice device,
                                                               final boolean deviceVisible) {
        final NetworkMonitorDeviceGroup deviceGroup = findDeviceGroupById(groupId);
        if (deviceGroup != null) {
            return deviceGroup;
        }
        return addDeviceGroup(groupId, type, deviceVisible);
    }

    private NetworkMonitorDeviceGroup addDeviceGroup(final MonitorNodeTypeId groupId,
                                                     final NetworkMonitorDeviceType type,
                                                     final boolean deviceVisible) {
        final NetworkMonitorDeviceGroup deviceGroup = NetworkMonitorDeviceGroup.create(groupId, type);
        deviceGroups.add(deviceGroup);
        if (deviceGroupFilter.test(deviceGroup) || deviceVisible) {
            visibleDeviceGroups.add(deviceGroup);
        }
        if (listener != null) {
            listener.onDeviceGroupAdded(deviceGroup);
        }
        return deviceGroup;
    }

    private void addDeviceToCategoryAndCreateCategoryIfItDoesNotExist(final NetworkMonitorDevice device,
                                                                      final NetworkMonitorDeviceCategory deviceCategory,
                                                                      final boolean visible) {
        final Set<NetworkMonitorDevice> devices = devicesByCategory.get(deviceCategory);
        if (devices == null) {
            addDeviceCategory(device, deviceCategory, visible);
        } else {
            devices.add(device);
        }
    }

    private void addDeviceCategory(final NetworkMonitorDevice device, final NetworkMonitorDeviceCategory deviceCategory,
                                   final boolean deviceVisible) {
        devicesByCategory.put(deviceCategory, new HashSet<>(Collections.singletonList(device)));
        if (deviceCategoryFilter.test(deviceCategory) || deviceVisible) {
            visibleDeviceCategories.add(deviceCategory);
        }
        if (listener != null) {
            listener.onDeviceCategoryAdded(deviceCategory);
        }
    }

    void onSearchTextChanged(final String text) {
        final String normalizedText = text.trim().toLowerCase(Locale.ROOT);
        searching = !normalizedText.isEmpty();
        updateFilters(normalizedText);
        updateDeviceVisibility();
    }

    void onViewTypeChanged() {
        updateDeviceVisibility();
    }

    private void updateFilters(final String normalizedText) {
        deviceGroupFilter = createDeviceGroupBaseFilter().and(group -> normalizedText.isEmpty()
            || group.type().name().getString().trim().toLowerCase(Locale.ROOT).contains(normalizedText));
        deviceCategoryFilter = createDeviceCategoryBaseFilter().and(category -> normalizedText.isEmpty()
            || deviceCategoryTranslations.get(category).trim().toLowerCase(Locale.ROOT).contains(normalizedText));
        deviceFilter = createDeviceBaseFilter().and((group, device) -> normalizedText.isEmpty()
            || device.name().getString().trim().toLowerCase(Locale.ROOT).contains(normalizedText));
    }

    boolean isVisible(final NetworkMonitorDeviceGroup deviceGroup) {
        return visibleDeviceGroups.contains(deviceGroup);
    }

    boolean isVisible(final NetworkMonitorDeviceCategory deviceCategory) {
        return visibleDeviceCategories.contains(deviceCategory);
    }

    boolean isVisible(final NetworkMonitorDevice device) {
        return visibleDevices.contains(device);
    }

    boolean isSearching() {
        return searching;
    }

    record DeviceRemovalSideEffects(@Nullable NetworkMonitorDeviceGroup removedDeviceGroup,
                                    @Nullable NetworkMonitorDeviceCategory removedDeviceCategory) {
        private static final DeviceRemovalSideEffects NONE = new DeviceRemovalSideEffects(null, null);
    }
}
