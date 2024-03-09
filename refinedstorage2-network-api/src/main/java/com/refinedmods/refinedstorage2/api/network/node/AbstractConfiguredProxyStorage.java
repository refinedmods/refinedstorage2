package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.Priority;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public abstract class AbstractConfiguredProxyStorage<S extends Storage> implements Storage, Priority {
    @Nullable
    private S delegate;
    private final StorageConfiguration config;

    protected AbstractConfiguredProxyStorage(final StorageConfiguration config) {
        this.config = config;
    }

    protected AbstractConfiguredProxyStorage(final StorageConfiguration config, final S delegate) {
        this(config);
        this.delegate = delegate;
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        if (delegate == null || config.getAccessMode() == AccessMode.INSERT || !config.isActive()) {
            return 0;
        }
        return delegate.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        if (delegate == null
            || config.getAccessMode() == AccessMode.EXTRACT
            || !config.isActive()
            || !config.isAllowed(resource)) {
            return 0;
        }
        return delegate.insert(resource, amount, action, actor);
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        return delegate == null ? Collections.emptySet() : delegate.getAll();
    }

    @Override
    public long getStored() {
        return delegate == null ? 0L : delegate.getStored();
    }

    @Override
    public int getPriority() {
        return config.getPriority();
    }

    protected S getDelegate() {
        return Objects.requireNonNull(getUnsafeDelegate());
    }

    @Nullable
    protected S getUnsafeDelegate() {
        return delegate;
    }

    public void setDelegate(final S newDelegate) {
        CoreValidations.validateNull(this.delegate, "The current delegate is still set");
        CoreValidations.validateNotNull(newDelegate, "The new delegate cannot be null");
        this.delegate = newDelegate;
    }

    public final void tryClearDelegate() {
        if (delegate == null) {
            return;
        }
        clearDelegate();
    }

    public void clearDelegate() {
        CoreValidations.validateNotNull(delegate, "There is no delegate set, cannot clear");
        this.delegate = null;
    }
}
