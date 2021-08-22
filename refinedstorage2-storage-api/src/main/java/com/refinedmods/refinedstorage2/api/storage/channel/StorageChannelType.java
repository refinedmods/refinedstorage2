package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.stack.Rs2Stack;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;

import java.util.List;

public interface StorageChannelType<T extends Rs2Stack> {
    StorageChannel<T> create();

    CompositeStorage<T> createEmptyCompositeStorage();

    CompositeStorage<T> createCompositeStorage(List<Storage<T>> sources);
}
