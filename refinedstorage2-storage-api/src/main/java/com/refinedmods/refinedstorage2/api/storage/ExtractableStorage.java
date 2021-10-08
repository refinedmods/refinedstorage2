package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;

public interface ExtractableStorage<T> {
    long extract(T resource, long amount, Action action);
}
