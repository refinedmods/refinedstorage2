package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.Priority;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;

public abstract class AbstractConfiguredProxyStorage<T, S extends Storage<T>> implements Storage<T>, Priority {
    @Nullable
    protected S delegate;
    private final StorageConfiguration config;

    protected AbstractConfiguredProxyStorage(final StorageConfiguration config) {
        this.config = config;
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        if (delegate == null || config.getAccessMode() == AccessMode.INSERT || !config.isActive()) {
            return 0;
        }
        return delegate.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        if (delegate == null
            || config.getAccessMode() == AccessMode.EXTRACT
            || !config.isActive()
            || !config.isAllowed(resource)) {
            return 0;
        }
        return delegate.insert(resource, amount, action, actor);
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
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

    public void setDelegate(final S newDelegate) {
        CoreValidations.validateNull(this.delegate, "The current delegate is still set");
        CoreValidations.validateNotNull(newDelegate, "The new delegate cannot be null");
        this.delegate = newDelegate;
    }
}
