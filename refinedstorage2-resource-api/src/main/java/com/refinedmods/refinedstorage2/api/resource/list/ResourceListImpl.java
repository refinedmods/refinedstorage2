package com.refinedmods.refinedstorage2.api.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    @Override
    public ResourceListOperationResult<T> add(final T resource, final long amount) {
        final ResourceAmount<T> existing = entries.get(resource);
        if (existing != null) {
            return addToExisting(existing, amount);
        } else {
            return addNew(resource, amount);
        }
    }

    private ResourceListOperationResult<T> addToExisting(final ResourceAmount<T> resourceAmount, final long amount) {
        resourceAmount.increment(amount);

        return new ResourceListOperationResult<>(resourceAmount, amount, true);
    }

    private ResourceListOperationResult<T> addNew(final T resource, final long amount) {
        final ResourceAmount<T> resourceAmount = new ResourceAmount<>(resource, amount);
        entries.put(resource, resourceAmount);
        return new ResourceListOperationResult<>(resourceAmount, amount, true);
    }

    @Override
    public Optional<ResourceListOperationResult<T>> remove(final T resource, final long amount) {
        ResourceAmount.validate(resource, amount);

        final ResourceAmount<T> existing = entries.get(resource);
        if (existing != null) {
            if (existing.getAmount() - amount <= 0) {
                return removeCompletely(existing);
            } else {
                return removePartly(amount, existing);
            }
        }

        return Optional.empty();
    }

    private Optional<ResourceListOperationResult<T>> removePartly(final long amount,
                                                                  final ResourceAmount<T> resourceAmount) {
        resourceAmount.decrement(amount);

        return Optional.of(new ResourceListOperationResult<>(resourceAmount, -amount, true));
    }

    private Optional<ResourceListOperationResult<T>> removeCompletely(final ResourceAmount<T> resourceAmount) {
        entries.remove(resourceAmount.getResource());

        return Optional.of(new ResourceListOperationResult<>(
            resourceAmount,
            -resourceAmount.getAmount(),
            false
        ));
    }

    @Override
    public Optional<ResourceAmount<T>> get(final T resource) {
        return Optional.ofNullable(entries.get(resource));
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return entries.values();
    }

    @Override
    public void clear() {
        entries.clear();
    }
}
