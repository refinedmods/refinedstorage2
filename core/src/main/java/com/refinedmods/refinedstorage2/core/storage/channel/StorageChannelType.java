package com.refinedmods.refinedstorage2.core.storage.channel;

import com.refinedmods.refinedstorage2.core.stack.Rs2Stack;
import com.refinedmods.refinedstorage2.core.storage.Storage;
import com.refinedmods.refinedstorage2.core.storage.composite.CompositeStorage;

import java.util.List;

public interface StorageChannelType<T extends Rs2Stack> {
    StorageChannel<T> create();

    CompositeStorage<T> createEmptyCompositeStorage();

    CompositeStorage<T> createCompositeStorage(List<Storage<T>> sources);
}
