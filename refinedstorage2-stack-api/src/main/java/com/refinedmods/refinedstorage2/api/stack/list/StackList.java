package com.refinedmods.refinedstorage2.api.stack.list;

import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

// TODO: Rename to ResourceList
// TODO: Rename to resource-api
public interface StackList<T> {
    StackListResult<T> add(T resource, long amount);

    default StackListResult<T> add(ResourceAmount<T> resourceAmount) {
        return add(resourceAmount.getResource(), resourceAmount.getAmount());
    }

    Optional<StackListResult<T>> remove(T resource, long amount);

    Optional<ResourceAmount<T>> get(T resource);

    Optional<ResourceAmount<T>> get(UUID id);

    Collection<ResourceAmount<T>> getAll();

    void clear();
}
