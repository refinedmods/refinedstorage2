package com.refinedmods.refinedstorage.api.network.impl.node.storage;

import com.refinedmods.refinedstorage.api.network.impl.storage.AbstractImmutableConfiguredProxyStorage;
import com.refinedmods.refinedstorage.api.network.impl.storage.StorageConfiguration;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.composite.CompositeStorage;
import com.refinedmods.refinedstorage.api.storage.composite.CompositeStorageImpl;
import com.refinedmods.refinedstorage.api.storage.composite.ParentComposite;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;

import java.util.List;
import java.util.Optional;

class ExposedStorage extends AbstractImmutableConfiguredProxyStorage<CompositeStorageImpl> implements CompositeStorage {
    protected ExposedStorage(final StorageConfiguration config) {
        super(config, new CompositeStorageImpl(new ResourceListImpl()));
    }

    long getCapacity() {
        final CompositeStorageImpl delegate = getUnsafeDelegate();
        if (delegate == null) {
            return 0;
        }
        return delegate.getSources()
            .stream()
            .filter(LimitedStorage.class::isInstance)
            .map(LimitedStorage.class::cast)
            .mapToLong(LimitedStorage::getCapacity)
            .sum();
    }

    @Override
    public void sortSources() {
        // no-op: cannot sort individual storages.
    }

    @Override
    public void addSource(final Storage source) {
        getDelegate().addSource(source);
    }

    @Override
    public void removeSource(final Storage source) {
        getDelegate().removeSource(source);
    }

    @Override
    public List<Storage> getSources() {
        return getDelegate().getSources();
    }

    @Override
    public void clearSources() {
        getDelegate().clearSources();
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final ResourceKey resource,
                                                                    final Class<? extends Actor> actorType) {
        return getDelegate().findTrackedResourceByActorType(resource, actorType);
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite parentComposite) {
        getDelegate().onAddedIntoComposite(parentComposite);
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite parentComposite) {
        getDelegate().onRemovedFromComposite(parentComposite);
    }
}
