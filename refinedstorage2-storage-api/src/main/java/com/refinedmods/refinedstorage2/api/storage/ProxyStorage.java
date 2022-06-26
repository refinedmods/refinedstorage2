package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Collection;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;

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
    protected ProxyStorage(final Storage<T> delegate) {
        Preconditions.checkNotNull(delegate);
        this.delegate = delegate;
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Source source) {
        return delegate.extract(resource, amount, action, source);
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Source source) {
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
