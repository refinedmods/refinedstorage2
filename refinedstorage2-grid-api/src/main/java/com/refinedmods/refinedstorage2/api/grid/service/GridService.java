package com.refinedmods.refinedstorage2.api.grid.service;

import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;

import java.util.Optional;

public interface GridService<T> {
    Optional<ResourceAmount<T>> insert(ResourceAmount<T> resourceAmount, GridInsertMode insertMode);
}
