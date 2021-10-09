package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;

import java.util.List;

public interface StorageChannelType<T> {
    StorageChannel<T> create();

    CompositeStorage<T> createEmptyCompositeStorage();

    CompositeStorage<T> createCompositeStorage(List<Storage<T>> sources);
}
