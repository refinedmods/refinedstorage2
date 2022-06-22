package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

@FunctionalInterface
public interface DiskDriveListener {
    void onDiskChanged();
}
