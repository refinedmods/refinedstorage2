package com.refinedmods.refinedstorage2.api.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ResourceList<T> {
    ResourceListOperationResult<T> add(T resource, long amount);

    default ResourceListOperationResult<T> add(ResourceAmount<T> resourceAmount) {
        return add(resourceAmount.getResource(), resourceAmount.getAmount());
    }

    Optional<ResourceListOperationResult<T>> remove(T resource, long amount);

    Optional<ResourceAmount<T>> get(T resource);

    Optional<ResourceAmount<T>> get(UUID id);

    Collection<ResourceAmount<T>> getAll();

    void clear();
}
