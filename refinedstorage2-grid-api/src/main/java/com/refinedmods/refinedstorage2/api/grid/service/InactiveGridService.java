package com.refinedmods.refinedstorage2.api.grid.service;

import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;

import java.util.Optional;

public class InactiveGridService<T> implements GridService<T> {
    @Override
    public Optional<ResourceAmount<T>> insert(ResourceAmount<T> resourceAmount, GridInsertMode insertMode) {
        return Optional.of(resourceAmount);
    }
}
