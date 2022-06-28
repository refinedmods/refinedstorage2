package com.refinedmods.refinedstorage2.api.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apiguardian.api.API;

/**
 * An implementation of a {@link ResourceList} that stores the resource entries in a {@link HashMap}.
 * This resource list implementation relies on {@link Object#equals(Object)} and {@link Object#hashCode()}.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public class ResourceListImpl<T> implements ResourceList<T> {
    private final Map<T, ResourceAmount<T>> entries = new HashMap<>();
    private final BiMap<UUID, ResourceAmount<T>> index = HashBiMap.create();

    @Override
    public ResourceListOperationResult<T> add(final T resource, final long amount) {
        ResourceAmount.validate(resource, amount);

        final ResourceAmount<T> existing = entries.get(resource);
        if (existing != null) {
            return addToExisting(existing, amount);
        } else {
            return addNew(resource, amount);
        }
    }

    private ResourceListOperationResult<T> addToExisting(final ResourceAmount<T> resourceAmount, final long amount) {
        resourceAmount.increment(amount);

        return new ResourceListOperationResult<>(resourceAmount, amount, index.inverse().get(resourceAmount), true);
    }

    private ResourceListOperationResult<T> addNew(final T resource, final long amount) {
        final ResourceAmount<T> resourceAmount = new ResourceAmount<>(resource, amount);

        final UUID id = UUID.randomUUID();

        index.put(id, resourceAmount);
        entries.put(resource, resourceAmount);

        return new ResourceListOperationResult<>(resourceAmount, amount, id, true);
    }

    @Override
    public Optional<ResourceListOperationResult<T>> remove(final T resource, final long amount) {
        ResourceAmount.validate(resource, amount);

        final ResourceAmount<T> existing = entries.get(resource);
        if (existing != null) {
            final UUID id = index.inverse().get(existing);

            if (existing.getAmount() - amount <= 0) {
                return removeCompletely(existing, id);
            } else {
                return removePartly(amount, existing, id);
            }
        }

        return Optional.empty();
    }

    private Optional<ResourceListOperationResult<T>> removePartly(final long amount,
                                                                  final ResourceAmount<T> resourceAmount,
                                                                  final UUID id) {
        resourceAmount.decrement(amount);

        return Optional.of(new ResourceListOperationResult<>(resourceAmount, -amount, id, true));
    }

    private Optional<ResourceListOperationResult<T>> removeCompletely(final ResourceAmount<T> resourceAmount,
                                                                      final UUID id) {
        index.remove(id);
        entries.remove(resourceAmount.getResource());

        return Optional.of(new ResourceListOperationResult<>(
                resourceAmount,
                -resourceAmount.getAmount(),
                id,
                false
        ));
    }

    @Override
    public Optional<ResourceAmount<T>> get(final T resource) {
        return Optional.ofNullable(entries.get(resource));
    }

    @Override
    public Optional<ResourceAmount<T>> get(final UUID id) {
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
