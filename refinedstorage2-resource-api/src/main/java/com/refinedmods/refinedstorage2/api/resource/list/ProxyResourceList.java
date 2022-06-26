package com.refinedmods.refinedstorage2.api.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.apiguardian.api.API;

/**
 * This is a utility class to easily decorate a {@link ResourceList}.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public abstract class ProxyResourceList<T> implements ResourceList<T> {
    private final ResourceList<T> delegate;

    protected ProxyResourceList(final ResourceList<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public ResourceListOperationResult<T> add(final T resource, final long amount) {
        return delegate.add(resource, amount);
    }

    @Override
    public Optional<ResourceListOperationResult<T>> remove(final T resource, final long amount) {
        return delegate.remove(resource, amount);
    }

    @Override
    public Optional<ResourceAmount<T>> get(final T resource) {
        return delegate.get(resource);
    }

    @Override
    public Optional<ResourceAmount<T>> get(final UUID id) {
        return delegate.get(id);
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return delegate.getAll();
    }

    @Override
    public void clear() {
        delegate.clear();
    }
}
