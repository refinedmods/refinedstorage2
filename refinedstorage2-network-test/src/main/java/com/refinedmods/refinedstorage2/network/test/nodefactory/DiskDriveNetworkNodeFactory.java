package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.impl.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.Map;

public class DiskDriveNetworkNodeFactory extends AbstractNetworkNodeFactory<DiskDriveNetworkNode> {
    public static final String PROPERTY_ENERGY_USAGE_PER_DISK = "energy_usage_per_disk";
    public static final String PROPERTY_DISK_COUNT = "disk_count";

    @Override
    protected DiskDriveNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        final long energyUsagePerDisk = (long) properties.getOrDefault(PROPERTY_ENERGY_USAGE_PER_DISK, 0L);
        final int diskCount = (int) properties.getOrDefault(PROPERTY_DISK_COUNT, 9);
        return new DiskDriveNetworkNode(
            getEnergyUsage(properties),
            energyUsagePerDisk,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE_REGISTRY,
            diskCount
        );
    }
}
