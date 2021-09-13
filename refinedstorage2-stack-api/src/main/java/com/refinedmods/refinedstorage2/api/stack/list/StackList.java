package com.refinedmods.refinedstorage2.api.stack.list;

import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

// TODO: Rename to ResourceList
// TODO: Rename to resource-api
public interface StackList<R> {
    StackListResult<R> add(R resource, long amount);

    Optional<StackListResult<R>> remove(R resource, long amount);

    Optional<ResourceAmount<R>> get(R resource);

    Optional<ResourceAmount<R>> get(UUID id);

    Collection<ResourceAmount<R>> getAll();

    void clear();
}
