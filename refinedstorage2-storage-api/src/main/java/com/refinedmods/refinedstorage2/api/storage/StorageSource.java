package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.stack.Rs2Stack;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Optional;

public interface StorageSource {
    <T extends Rs2Stack> Optional<Storage<T>> getStorageForChannel(StorageChannelType<T> channelType);
}
