package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Optional;

public interface StorageSource {
    <T> Optional<Storage<T>> getStorageForChannel(StorageChannelType<T> channelType);
}
