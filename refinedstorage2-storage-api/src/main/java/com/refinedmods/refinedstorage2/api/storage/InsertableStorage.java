package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;

public interface InsertableStorage<T> {
    long insert(T resource, long amount, Action action);
}
