package com.refinedmods.refinedstorage2.api.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class ResourceListImpl<T> implements ResourceList<T> {
    private final Map<T, ResourceAmount<T>> entries = new HashMap<>();
    private final BiMap<UUID, ResourceAmount<T>> index = HashBiMap.create();

    @Override
    public ResourceListOperationResult<T> add(T resource, long amount) {
        ResourceAmount.validate(resource, amount);

        ResourceAmount<T> existing = entries.get(resource);
        if (existing != null) {
            return addToExisting(existing, amount);
        } else {
            return addNew(resource, amount);
        }
    }

    private ResourceListOperationResult<T> addToExisting(ResourceAmount<T> resourceAmount, long amount) {
        resourceAmount.increment(amount);

        return new ResourceListOperationResult<>(resourceAmount, amount, index.inverse().get(resourceAmount), true);
    }

    private ResourceListOperationResult<T> addNew(T resource, long amount) {
        ResourceAmount<T> resourceAmount = new ResourceAmount<>(resource, amount);

        UUID id = UUID.randomUUID();

        index.put(id, resourceAmount);
        entries.put(resource, resourceAmount);

        return new ResourceListOperationResult<>(resourceAmount, amount, id, true);
    }

    @Override
    public Optional<ResourceListOperationResult<T>> remove(T resource, long amount) {
        ResourceAmount.validate(resource, amount);

        ResourceAmount<T> existing = entries.get(resource);
        if (existing != null) {
            UUID id = index.inverse().get(existing);

            if (existing.getAmount() - amount <= 0) {
                return removeCompletely(existing, id);
            } else {
                return removePartly(amount, existing, id);
            }
        }

        return Optional.empty();
    }

    private Optional<ResourceListOperationResult<T>> removePartly(long amount, ResourceAmount<T> resourceAmount, UUID id) {
        resourceAmount.decrement(amount);

        return Optional.of(new ResourceListOperationResult<>(resourceAmount, -amount, id, true));
    }

    private Optional<ResourceListOperationResult<T>> removeCompletely(ResourceAmount<T> resourceAmount, UUID id) {
        index.remove(id);
        entries.remove(resourceAmount.getResource());

        return Optional.of(new ResourceListOperationResult<>(resourceAmount, -resourceAmount.getAmount(), id, false));
    }

    @Override
    public Optional<ResourceAmount<T>> get(T resource) {
        return Optional.ofNullable(entries.get(resource));
    }

    @Override
    public Optional<ResourceAmount<T>> get(UUID id) {
        return Optional.ofNullable(index.get(id));
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return entries.values();
    }

    @Override
    public void clear() {
        index.clear();
        entries.clear();
    }
}
