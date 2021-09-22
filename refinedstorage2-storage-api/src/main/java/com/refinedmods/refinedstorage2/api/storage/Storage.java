package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;

public interface Storage<T> extends StorageView<T>, InsertableStorage<T> {
    long extract(T resource, long amount, Action action);
}
