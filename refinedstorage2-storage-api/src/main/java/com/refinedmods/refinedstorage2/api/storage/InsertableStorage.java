package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;

public interface InsertableStorage<T> {
    /**
     * Inserts a resource into a storage.
     *
     * @param resource the resource, may not be null
     * @param amount   the amount, must be larger than 0
     * @param action   the mode of insertion
     * @return the remainder (the amount not inserted)
     */
    long insert(T resource, long amount, Action action);
}
