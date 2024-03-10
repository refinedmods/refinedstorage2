package com.refinedmods.refinedstorage2.api.network.impl.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeAwareChild;
import com.refinedmods.refinedstorage2.api.storage.composite.Priority;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public abstract class AbstractConfiguredProxyStorage<S extends Storage> implements CompositeAwareChild, Priority {
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
        throw new UnsupportedOperationException("Immediate extract is not allowed");
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        throw new UnsupportedOperationException("Immediate insert is not allowed");
    }

    @Override
    public Amount compositeInsert(final ResourceKey resource,
                                  final long amount,
                                  final Action action,
                                  final Actor actor) {
        if (delegate == null || config.isExtractOnly() || !config.isActive() || !config.isAllowed(resource)) {
            return Amount.ZERO;
        }
        final long inserted = delegate.insert(resource, amount, action, actor);
        if ((config.isVoidExcess() && config.getFilterMode() == FilterMode.ALLOW) && inserted < amount) {
            return new Amount(amount, inserted);
        }
        return new Amount(inserted, inserted);
    }

    @Override
    public Amount compositeExtract(final ResourceKey resource,
                                   final long amount,
                                   final Action action,
                                   final Actor actor) {
        if (delegate == null || config.isInsertOnly() || !config.isActive()) {
            return Amount.ZERO;
        }
        final long extracted = delegate.extract(resource, amount, action, actor);
        return new Amount(extracted, extracted);
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
