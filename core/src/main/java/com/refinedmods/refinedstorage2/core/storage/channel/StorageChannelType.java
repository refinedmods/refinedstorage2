package com.refinedmods.refinedstorage2.core.storage.channel;

@FunctionalInterface
public interface StorageChannelType<T> {
    StorageChannel<T> create();
}
