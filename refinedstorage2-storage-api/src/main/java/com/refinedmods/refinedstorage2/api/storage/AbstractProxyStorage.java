package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Collection;

import org.apiguardian.api.API;

/**
 * This is a utility class to easily decorate a {@link Storage}.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public abstract class AbstractProxyStorage<T> implements Storage<T> {
    protected final Storage<T> delegate;

    /**
     * @param delegate the storage to delegate operations to, may not be null
     */
    protected AbstractProxyStorage(final Storage<T> delegate) {
        CoreValidations.validateNotNull(delegate, "Delegate must not be null");
        this.delegate = delegate;
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        return delegate.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        return delegate.insert(resource, amount, action, actor);
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
