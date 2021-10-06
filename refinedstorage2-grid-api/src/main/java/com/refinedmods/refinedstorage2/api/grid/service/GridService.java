package com.refinedmods.refinedstorage2.api.grid.service;

import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;

import java.util.Optional;

public interface GridService<T> {
    Optional<ResourceAmount<T>> insert(ResourceAmount<T> resourceAmount, GridInsertMode insertMode);

    void extract(T resource, GridExtractMode extractMode, InsertableStorage<T> destination);
}
