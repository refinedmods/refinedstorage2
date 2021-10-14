package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;

public interface ExtractableStorage<T> {
    /**
     * Extracts a resource from a storage.
     *
     * @param resource the resource, may not be null
     * @param amount   the amount, must be larger than 0
     * @param action   the mode of extraction
     * @return the amount extracted
     */
    long extract(T resource, long amount, Action action);
}
