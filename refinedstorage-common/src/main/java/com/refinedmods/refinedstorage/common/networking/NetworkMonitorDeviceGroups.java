package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeId;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeTypeId;
import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceType;

import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

class NetworkMonitorDeviceGroups {
    private final List<NetworkMonitorDeviceGroup> deviceGroups;
    private final List<NetworkMonitorDeviceGroup> deviceGroupsView;
    @Nullable
    private NetworkMonitorListener listener;

    NetworkMonitorDeviceGroups(final List<NetworkMonitorDeviceGroup> deviceGroups) {
        this.deviceGroups = deviceGroups;
        this.deviceGroupsView = Collections.unmodifiableList(deviceGroups);
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
        deviceGroups.remove(deviceGroup);
        if (listener != null) {
            listener.onDeviceGroupRemoved(deviceGroup);
        }
    }

    void addDevice(final MonitorNodeTypeId groupId, final NetworkMonitorDeviceType type,
                   final NetworkMonitorDevice device) {
        final NetworkMonitorDeviceGroup deviceGroup = findOrAdd(groupId, type);
        deviceGroup.devices().add(device);
        if (listener != null) {
            listener.onDeviceAdded(deviceGroup, device);
        }
    }

    private NetworkMonitorDeviceGroup findOrAdd(final MonitorNodeTypeId groupId, final NetworkMonitorDeviceType type) {
        for (final NetworkMonitorDeviceGroup deviceGroup : deviceGroups) {
            if (deviceGroup.id().equals(groupId.id())) {
                return deviceGroup;
            }
        }
        return add(groupId, type);
    }

    private NetworkMonitorDeviceGroup add(final MonitorNodeTypeId groupId, final NetworkMonitorDeviceType type) {
        final NetworkMonitorDeviceGroup deviceGroup = NetworkMonitorDeviceGroup.create(groupId, type);
        deviceGroups.add(deviceGroup);
        if (listener != null) {
            listener.onDeviceGroupAdded(deviceGroup);
        }
        return deviceGroup;
    }

    @Nullable
    private NetworkMonitorDeviceGroup findContainingDeviceById(final MonitorNodeId id) {
        return deviceGroups.stream()
            .filter(deviceGroup -> deviceGroup.hasDevice(id))
            .findFirst()
            .orElse(null);
    }
}
