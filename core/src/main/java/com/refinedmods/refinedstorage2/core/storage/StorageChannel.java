package com.refinedmods.refinedstorage2.core.storage;

public interface StorageChannel<T> extends Storage<T> {
    void addListener(StorageChannelListener<T> listener);

    void removeListener(StorageChannelListener<T> listener);
}
