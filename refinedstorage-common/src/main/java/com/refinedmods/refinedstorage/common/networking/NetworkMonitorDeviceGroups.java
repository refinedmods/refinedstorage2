package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeId;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeTypeId;
import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceType;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jspecify.annotations.Nullable;

class NetworkMonitorDeviceGroups {
    private final List<NetworkMonitorDeviceGroup> deviceGroups;
    private final List<NetworkMonitorDeviceGroup> deviceGroupsView;
    private final Set<NetworkMonitorDeviceGroup> visibleDeviceGroups = new HashSet<>();
    private final Set<NetworkMonitorDevice> visibleDevices = new HashSet<>();
    @Nullable
    private NetworkMonitorListener listener;
    private String lastSearchQuery = "";

    NetworkMonitorDeviceGroups(final List<NetworkMonitorDeviceGroup> deviceGroups) {
        this.deviceGroups = deviceGroups;
        this.deviceGroupsView = Collections.unmodifiableList(deviceGroups);
        for (final NetworkMonitorDeviceGroup deviceGroup : deviceGroups) {
            visibleDeviceGroups.add(deviceGroup);
            visibleDevices.addAll(deviceGroup.devices());
        }
    }

    void setListener(final NetworkMonitorListener listener) {
        this.listener = listener;
    }

    List<NetworkMonitorDeviceGroup> getAll() {
        return deviceGroupsView;
    }

    @Nullable
    NetworkMonitorDeviceGroup removeDevice(final MonitorNodeId id) {
        final NetworkMonitorDeviceGroup deviceGroup = findContainingDeviceById(id);
        if (deviceGroup == null) {
            return null;
        }
        final NetworkMonitorDevice device = deviceGroup.findDeviceById(id);
        if (device == null) {
            return null;
        }
        deviceGroup.devices().remove(device);
        visibleDevices.remove(device);
        if (listener != null) {
            listener.onDeviceRemoved(deviceGroup, device);
        }
        if (deviceGroup.devices().isEmpty()) {
            remove(deviceGroup);
            return deviceGroup;
        }
        return null;
    }

    private void remove(final NetworkMonitorDeviceGroup deviceGroup) {
        visibleDeviceGroups.remove(deviceGroup);
        deviceGroups.remove(deviceGroup);
        if (listener != null) {
            listener.onDeviceGroupRemoved(deviceGroup);
        }
    }

    void addDevice(final MonitorNodeTypeId groupId, final NetworkMonitorDeviceType type,
                   final NetworkMonitorDevice device) {
        final NetworkMonitorDeviceGroup deviceGroup = findById(groupId);
        if (deviceGroup == null) {
            add(groupId, type, device);
            return;
        }
        deviceGroup.devices().add(device);
        final boolean deviceVisible = isDeviceVisible(device, lastSearchQuery);
        if (deviceVisible) {
            visibleDeviceGroups.add(deviceGroup);
            visibleDevices.add(device);
        }
        if (listener != null) {
            listener.onDeviceAdded(deviceGroup, device);
        }
    }

    @Nullable
    private NetworkMonitorDeviceGroup findById(final MonitorNodeTypeId groupId) {
        for (final NetworkMonitorDeviceGroup deviceGroup : deviceGroups) {
            if (deviceGroup.id().equals(groupId.id())) {
                return deviceGroup;
            }
        }
        return null;
    }

    private void add(final MonitorNodeTypeId groupId, final NetworkMonitorDeviceType type,
                     final NetworkMonitorDevice initialDevice) {
        final NetworkMonitorDeviceGroup deviceGroup = NetworkMonitorDeviceGroup.create(groupId, type, initialDevice);
        deviceGroups.add(deviceGroup);
        final boolean deviceVisible = isDeviceVisible(initialDevice, lastSearchQuery);
        if (isGroupVisible(deviceGroup, lastSearchQuery) || deviceVisible) {
            visibleDeviceGroups.add(deviceGroup);
        }
        if (deviceVisible) {
            visibleDevices.add(initialDevice);
        }
        if (listener != null) {
            listener.onDeviceGroupAdded(deviceGroup);
        }
    }

    @Nullable
    private NetworkMonitorDeviceGroup findContainingDeviceById(final MonitorNodeId id) {
        return deviceGroups.stream()
            .filter(deviceGroup -> deviceGroup.hasDevice(id))
            .findFirst()
            .orElse(null);
    }

    void onSearchTextChanged(final String text) {
        lastSearchQuery = text;
        visibleDeviceGroups.clear();
        visibleDevices.clear();
        final String normalizedText = text.trim().toLowerCase(Locale.ROOT);
        if (normalizedText.isEmpty()) {
            for (final NetworkMonitorDeviceGroup deviceGroup : deviceGroups) {
                visibleDeviceGroups.add(deviceGroup);
                visibleDevices.addAll(deviceGroup.devices());
            }
            return;
        }
        for (final NetworkMonitorDeviceGroup deviceGroup : deviceGroups) {
            if (onSearchTextChanged(deviceGroup, normalizedText)) {
                visibleDeviceGroups.add(deviceGroup);
            }
        }
    }

    boolean onSearchTextChanged(final NetworkMonitorDeviceGroup deviceGroup, final String normalizedText) {
        boolean groupVisible = isGroupVisible(deviceGroup, normalizedText);
        for (final NetworkMonitorDevice device : deviceGroup.devices()) {
            if (isDeviceVisible(device, normalizedText)) {
                visibleDevices.add(device);
                groupVisible = true;
            }
        }
        return groupVisible;
    }

    private static boolean isGroupVisible(final NetworkMonitorDeviceGroup deviceGroup, final String normalizedText) {
        return deviceGroup.type().name().getString().trim().toLowerCase(Locale.ROOT).contains(normalizedText);
    }

    private static boolean isDeviceVisible(final NetworkMonitorDevice device, final String normalizedText) {
        return device.name().getString().trim().toLowerCase(Locale.ROOT).contains(normalizedText);
    }

    boolean isVisible(final NetworkMonitorDeviceGroup deviceGroup) {
        return visibleDeviceGroups.contains(deviceGroup);
    }

    boolean isVisible(final NetworkMonitorDevice device) {
        return visibleDevices.contains(device);
    }
}
