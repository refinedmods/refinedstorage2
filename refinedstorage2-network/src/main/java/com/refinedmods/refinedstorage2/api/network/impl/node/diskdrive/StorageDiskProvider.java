package com.refinedmods.refinedstorage2.api.network.impl.node.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Optional;
import java.util.UUID;

public interface StorageDiskProvider {
    Optional<UUID> getDiskId(int slot);

    Optional<StorageChannelType<?>> getStorageChannelType(int slot);
}
