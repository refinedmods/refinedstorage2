package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;

import org.jspecify.annotations.Nullable;

interface NetworkMonitorListener {
    void onCurrentDeviceGroupChanged(NetworkMonitorDeviceGroup deviceGroup);

    void onCurrentDeviceChanged(NetworkMonitorDeviceGroup deviceGroup, NetworkMonitorDevice device);

    void onDeviceGroupAdded(NetworkMonitorDeviceGroup deviceGroup);

    void onDeviceGroupRemoved(NetworkMonitorDeviceGroup deviceGroup);

    void onDeviceAdded(NetworkMonitorDeviceGroup deviceGroup, NetworkMonitorDevice device);

    void onDeviceRemoved(NetworkMonitorDeviceGroup deviceGroup, NetworkMonitorDevice device);

    void onDetailsChanged(@Nullable NetworkNodeDetails details);
}
