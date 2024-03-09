package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;

import java.util.Collection;

import org.apiguardian.api.API;

/**
 * This is a utility class to easily decorate a {@link Storage}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public abstract class AbstractProxyStorage implements Storage {
    protected final Storage delegate;

    /**
     * @param delegate the storage to delegate operations to, may not be null
     */
    protected AbstractProxyStorage(final Storage delegate) {
        CoreValidations.validateNotNull(delegate, "Delegate must not be null");
        this.delegate = delegate;
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return delegate.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return delegate.insert(resource, amount, action, actor);
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        return delegate.getAll();
    }

    @Override
    public long getStored() {
        return delegate.getStored();
    }
}
