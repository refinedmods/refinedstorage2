package com.refinedmods.refinedstorage2.api.stack.list;

import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class StackListImpl<T> implements StackList<T> {
    private final Map<T, ResourceAmount<T>> entries = new HashMap<>();
    private final BiMap<UUID, ResourceAmount<T>> index = HashBiMap.create();

    @Override
    public StackListResult<T> add(T resource, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        ResourceAmount<T> existing = entries.get(resource);
        if (existing != null) {
            return addToExisting(existing, amount);
        } else {
            return addNew(resource, amount);
        }
    }

    private StackListResult<T> addToExisting(ResourceAmount<T> resourceAmount, long amount) {
        resourceAmount.increment(amount);

        return new StackListResult<>(resourceAmount, amount, index.inverse().get(resourceAmount), true);
    }

    private StackListResult<T> addNew(T resource, long amount) {
        ResourceAmount<T> resourceAmount = new ResourceAmount<>(resource, amount);

        UUID id = UUID.randomUUID();

        index.put(id, resourceAmount);
        entries.put(resource, resourceAmount);

        return new StackListResult<>(resourceAmount, amount, id, true);
    }

    @Override
    public Optional<StackListResult<T>> remove(T resource, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

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

    private Optional<StackListResult<T>> removePartly(long amount, ResourceAmount<T> resourceAmount, UUID id) {
        resourceAmount.decrement(amount);

        return Optional.of(new StackListResult<>(resourceAmount, -amount, id, true));
    }

    private Optional<StackListResult<T>> removeCompletely(ResourceAmount<T> resourceAmount, UUID id) {
        index.remove(id);
        entries.remove(resourceAmount.getResource());

        return Optional.of(new StackListResult<>(resourceAmount, -resourceAmount.getAmount(), id, false));
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
