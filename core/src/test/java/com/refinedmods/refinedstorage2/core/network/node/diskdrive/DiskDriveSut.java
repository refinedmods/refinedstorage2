package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import com.refinedmods.refinedstorage2.core.adapter.FakeRs2World;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.util.Position;

import static org.mockito.Mockito.mock;

public class DiskDriveSut {
    private final FakeStorageDiskProviderManager diskProviderManager = new FakeStorageDiskProviderManager();

    private final DiskDriveNetworkNode diskDrive = new DiskDriveNetworkNode(
            new FakeRs2World(),
            Position.ORIGIN,
            mock(NetworkNodeReference.class),
            diskProviderManager,
            diskProviderManager
    );

    public DiskDriveSut() {
        diskProviderManager.setDiskDrive(diskDrive);
    }

    public FakeStorageDiskProviderManager getDiskProviderManager() {
        return diskProviderManager;
    }

    public DiskDriveNetworkNode getDiskDrive() {
        return diskDrive;
    }
}
