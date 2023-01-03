package com.refinedmods.refinedstorage2.api.network.impl.node.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Optional;

// TODO: Rename to DiskDriveProvider
public interface StorageDiskProvider {
    Optional<Storage<?>> resolve(int slot);

    Optional<StorageChannelType<?>> getStorageChannelType(int slot);
}
