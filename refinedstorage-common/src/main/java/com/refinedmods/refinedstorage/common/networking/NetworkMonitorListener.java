package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceCategory;

import org.jspecify.annotations.Nullable;

interface NetworkMonitorListener {
    void onCurrentDeviceGroupChanged(@Nullable NetworkMonitorDeviceGroup deviceGroup);

    void onCurrentDeviceCategoryChanged(@Nullable NetworkMonitorDeviceCategory deviceCategory);

    void onCurrentDeviceChanged(@Nullable NetworkMonitorDevice device);

    void onDeviceGroupAdded(NetworkMonitorDeviceGroup deviceGroup);

    void onDeviceGroupRemoved(NetworkMonitorDeviceGroup deviceGroup);

    void onDeviceCategoryAdded(NetworkMonitorDeviceCategory deviceCategory);

    void onDeviceCategoryRemoved(NetworkMonitorDeviceCategory deviceCategory);

    void onDeviceAdded(NetworkMonitorDeviceGroup deviceGroup,
                       NetworkMonitorDeviceCategory deviceCategory,
                       NetworkMonitorDevice device);

    void onDeviceRemoved(NetworkMonitorDeviceGroup deviceGroup,
                         NetworkMonitorDeviceCategory deviceCategory,
                         NetworkMonitorDevice device);

    void onDetailsChanged(@Nullable NetworkMonitorDeviceGroup deviceGroup,
                          @Nullable NetworkMonitorDeviceCategory deviceCategory,
                          @Nullable NetworkMonitorDevice device,
                          @Nullable NetworkNodeDetails details);

    void onActiveChanged(boolean newActive);

    void onGroupTypeChanged(NetworkMonitorGroupType groupType);

    void onViewTypeChanged(@Nullable NetworkMonitorDeviceCategory viewType);

    void onSortingTypeChanged(NetworkMonitorSortingType sortingType);

    void onSortingDirectionChanged(NetworkMonitorSortingDirection sortingDirection);
}
