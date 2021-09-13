package com.refinedmods.refinedstorage2.api.stack.list;

import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class StackListImpl<R> implements StackList<R> {
    private final Map<R, ResourceAmount<R>> entries = new HashMap<>();
    private final BiMap<UUID, ResourceAmount<R>> index = HashBiMap.create();

    @Override
    public StackListResult<R> add(R resource, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        ResourceAmount<R> existing = entries.get(resource);
        if (existing != null) {
            return addToExisting(existing, amount);
        } else {
            return addNew(resource, amount);
        }
    }

    private StackListResult<R> addToExisting(ResourceAmount<R> resourceAmount, long amount) {
        resourceAmount.increment(amount);

        return new StackListResult<>(resourceAmount, amount, index.inverse().get(resourceAmount), true);
    }

    private StackListResult<R> addNew(R resource, long amount) {
        ResourceAmount<R> resourceAmount = new ResourceAmount<>(resource, amount);

        UUID id = UUID.randomUUID();

        index.put(id, resourceAmount);
        entries.put(resource, resourceAmount);

        return new StackListResult<>(resourceAmount, amount, id, true);
    }

    @Override
    public Optional<StackListResult<R>> remove(R resource, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        ResourceAmount<R> existing = entries.get(resource);
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

    private Optional<StackListResult<R>> removePartly(long amount, ResourceAmount<R> resourceAmount, UUID id) {
        resourceAmount.decrement(amount);

        return Optional.of(new StackListResult<>(resourceAmount, -amount, id, true));
    }

    private Optional<StackListResult<R>> removeCompletely(ResourceAmount<R> resourceAmount, UUID id) {
        index.remove(id);
        entries.remove(resourceAmount.getResource());

        return Optional.of(new StackListResult<>(resourceAmount, -resourceAmount.getAmount(), id, false));
    }

    @Override
    public Optional<ResourceAmount<R>> get(R resource) {
        return Optional.ofNullable(entries.get(resource));
    }

    @Override
    public Optional<ResourceAmount<R>> get(UUID id) {
        return Optional.ofNullable(index.get(id));
    }

    @Override
    public Collection<ResourceAmount<R>> getAll() {
        return entries.values();
    }

    @Override
    public void clear() {
        index.clear();
        entries.clear();
    }
}
