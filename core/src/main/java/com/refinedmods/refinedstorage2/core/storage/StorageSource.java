package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.stack.Rs2Stack;
import com.refinedmods.refinedstorage2.core.storage.channel.StorageChannelType;

import java.util.Optional;

public interface StorageSource {
    <T extends Rs2Stack> Optional<Storage<T>> getStorageForChannel(StorageChannelType<T> channelType);
}
