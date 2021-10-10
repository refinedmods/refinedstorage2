package com.refinedmods.refinedstorage2.api.grid.service;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;

import java.util.Optional;

public interface GridService<T> {
    Optional<ResourceAmount<T>> insert(ResourceAmount<T> resourceAmount, GridInsertMode insertMode);

    boolean insert(ResourceAmount<T> resourceAmount);

    void extract(T resource, GridExtractMode extractMode, InsertableStorage<T> destination);
}
