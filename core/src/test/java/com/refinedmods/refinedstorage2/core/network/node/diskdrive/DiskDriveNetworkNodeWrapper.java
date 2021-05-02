package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.adapter.FakeRs2World;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.util.Position;

public class DiskDriveNetworkNodeWrapper extends DiskDriveNetworkNode {
    private final FakeStorageDiskProviderManager fakeStorageDiskProviderManager;

    private DiskDriveNetworkNodeWrapper(Rs2World world, Position pos, NetworkNodeReference ref, FakeStorageDiskProviderManager fakeStorageDiskProviderManager, long energyUsage, long energyUsagePerDisk) {
        super(world, pos, ref, fakeStorageDiskProviderManager, fakeStorageDiskProviderManager, energyUsage, energyUsagePerDisk);
        this.fakeStorageDiskProviderManager = fakeStorageDiskProviderManager;
        this.fakeStorageDiskProviderManager.setDiskDrive(this);
    }

    public FakeStorageDiskProviderManager getFakeStorageDiskProviderManager() {
        return fakeStorageDiskProviderManager;
    }

    public static DiskDriveNetworkNodeWrapper create(long energyUsage, long energyUsagePerDisk) {
        FakeStorageDiskProviderManager fakeStorageDiskProviderManager = new FakeStorageDiskProviderManager();
        return new DiskDriveNetworkNodeWrapper(
                new FakeRs2World(),
                Position.ORIGIN,
                null,
                fakeStorageDiskProviderManager,
                energyUsage,
                energyUsagePerDisk
        );
    }

    public static DiskDriveNetworkNodeWrapper create() {
        return create(0, 0);
    }
}
