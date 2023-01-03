package com.refinedmods.refinedstorage2.api.network.impl.node.multistorage;

import com.refinedmods.refinedstorage2.api.network.node.AbstractImmutableConfiguredProxyStorage;
import com.refinedmods.refinedstorage2.api.network.node.StorageConfiguration;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeAwareChild;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.composite.ParentComposite;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.List;
import java.util.Optional;

class MultiStorageExposedStorage<T> extends AbstractImmutableConfiguredProxyStorage<T, CompositeStorageImpl<T>>
    implements CompositeStorage<T>, CompositeAwareChild<T> {
    protected MultiStorageExposedStorage(final StorageConfiguration config) {
        super(config, new CompositeStorageImpl<>(new ResourceListImpl<>()));
    }

    @Override
    public void sortSources() {
        // no-op: cannot sort individual storages.
    }

    @Override
    public void addSource(final Storage<T> source) {
        getDelegate().addSource(source);
    }

    @Override
    public void removeSource(final Storage<T> source) {
        getDelegate().removeSource(source);
    }

    @Override
    public List<Storage<T>> getSources() {
        return getDelegate().getSources();
    }

    @Override
    public void clearSources() {
        getDelegate().clearSources();
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final T resource,
                                                                    final Class<? extends Actor> actorType) {
        return getDelegate().findTrackedResourceByActorType(resource, actorType);
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite<T> parentComposite) {
        getDelegate().onAddedIntoComposite(parentComposite);
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite<T> parentComposite) {
        getDelegate().onRemovedFromComposite(parentComposite);
    }
}
