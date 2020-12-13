package com.refinedmods.refinedstorage2.core.storage;

import java.util.Optional;

public interface StorageChannel<T> extends Storage<T> {
    void addListener(StorageChannelListener<T> listener);

    void removeListener(StorageChannelListener<T> listener);

    Optional<T> get(T template);
}
