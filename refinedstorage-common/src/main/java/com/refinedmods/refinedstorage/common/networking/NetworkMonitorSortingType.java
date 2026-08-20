package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceCategory;

import java.util.Comparator;
import java.util.function.Function;

public enum NetworkMonitorSortingType {
    NAME(
        Comparator.comparing(
            group -> group.type().name().getString()),
        devices -> Comparator.<NetworkMonitorDeviceCategory, String>comparing(
            category -> NetworkMonitorDevices.createDeviceCategoryTranslation(category)
                .getString()),
        Comparator.comparing(
            device -> device.name().getString())
    ),
    ENERGY_USAGE(
        Comparator.comparingLong(NetworkMonitorDeviceGroup::totalEnergyUsage),
        devices -> Comparator.comparingLong(devices::getTotalEnergyUsage),
        Comparator.comparingLong(NetworkMonitorDevice::energyUsage)
    ),
    INSERT_PRIORITY(
        Comparator.comparing(
            group -> group.type().name().getString()),
        devices -> Comparator.<NetworkMonitorDeviceCategory, String>comparing(
            category -> NetworkMonitorDevices.createDeviceCategoryTranslation(category)
                .getString()),
        Comparator.comparingInt(d -> d.insertPriority().orElse(0))
    ),
    EXTRACT_PRIORITY(
        Comparator.comparing(
            group -> group.type().name().getString()),
        devices -> Comparator.<NetworkMonitorDeviceCategory, String>comparing(
            category -> NetworkMonitorDevices.createDeviceCategoryTranslation(category)
                .getString()),
        Comparator.comparingInt(d -> d.extractPriority().orElse(0))
    );

    private final Comparator<NetworkMonitorDeviceGroup> deviceGroupComparator;
    private final Function<NetworkMonitorDevices, Comparator<NetworkMonitorDeviceCategory>> deviceCategoryComparator;
    private final Comparator<NetworkMonitorDevice> deviceComparator;

    NetworkMonitorSortingType(final Comparator<NetworkMonitorDeviceGroup> deviceGroupComparator,
                              final Function<NetworkMonitorDevices, Comparator<NetworkMonitorDeviceCategory>> deviceCategoryComparator,
                              final Comparator<NetworkMonitorDevice> deviceComparator) {
        this.deviceGroupComparator = deviceGroupComparator;
        this.deviceCategoryComparator = deviceCategoryComparator;
        this.deviceComparator = deviceComparator;
    }

    Comparator<NetworkMonitorDeviceGroup> getDeviceGroupComparator() {
        return deviceGroupComparator;
    }

    Function<NetworkMonitorDevices, Comparator<NetworkMonitorDeviceCategory>> getDeviceCategoryComparator() {
        return deviceCategoryComparator;
    }

    Comparator<NetworkMonitorDevice> getDeviceComparator() {
        return deviceComparator;
    }
}
