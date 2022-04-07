package com.refinedmods.refinedstorage2.api.storage;

import com.google.common.base.Preconditions;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import org.apiguardian.api.API;

import java.util.Collection;

/**
 * This is a utility class to easily decorate a {@link Storage}.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public abstract class ProxyStorage<T> implements Storage<T> {
    protected final Storage<T> delegate;

    /**
     * @param delegate the storage to delegate operations to, may not be null
     */
    protected ProxyStorage(Storage<T> delegate) {
        Preconditions.checkNotNull(delegate);
        this.delegate = delegate;
    }

    @Override
    public long extract(T resource, long amount, Action action, Source source) {
        return delegate.extract(resource, amount, action, source);
    }

    @Override
    public long insert(T resource, long amount, Action action, Source source) {
        return delegate.insert(resource, amount, action, source);
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return delegate.getAll();
    }

    @Override
    public long getStored() {
        return delegate.getStored();
    }
}
