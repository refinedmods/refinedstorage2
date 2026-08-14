package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;

import org.jspecify.annotations.Nullable;

interface NetworkMonitorListener {
    void onCurrentDeviceGroupChanged(@Nullable NetworkMonitorDeviceGroup deviceGroup);

    void onCurrentDeviceChanged(@Nullable NetworkMonitorDevice device);

    void onDeviceGroupAdded(NetworkMonitorDeviceGroup deviceGroup);

    void onDeviceGroupRemoved(NetworkMonitorDeviceGroup deviceGroup);

    void onDeviceAdded(NetworkMonitorDeviceGroup deviceGroup, NetworkMonitorDevice device);

    void onDeviceRemoved(NetworkMonitorDeviceGroup deviceGroup, NetworkMonitorDevice device);

    void onDetailsChanged(@Nullable NetworkMonitorDeviceGroup deviceGroup,
                          @Nullable NetworkMonitorDevice device,
                          @Nullable NetworkNodeDetails details);

    void onActiveChanged(boolean newActive);
}
