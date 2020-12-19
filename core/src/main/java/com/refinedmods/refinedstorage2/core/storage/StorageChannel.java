package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.list.StackListListener;

import java.util.Optional;

public interface StorageChannel<T> extends Storage<T> {
    void addListener(StackListListener<T> listener);

    void removeListener(StackListListener<T> listener);

    Optional<T> get(T template);
}
