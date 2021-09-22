package com.refinedmods.refinedstorage2.api.grid.service;

import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;

import java.util.Optional;

public class InactiveGridService<T> implements GridService<T> {
    @Override
    public Optional<ResourceAmount<T>> insert(ResourceAmount<T> resourceAmount, GridInsertMode insertMode) {
        return Optional.of(resourceAmount);
    }

    @Override
    public void extract(T resource, GridExtractMode extractMode, InsertableStorage<T> destination) {
    }
}
