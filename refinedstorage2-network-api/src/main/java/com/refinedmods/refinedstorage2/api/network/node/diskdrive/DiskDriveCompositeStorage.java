package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

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

import java.util.Objects;
import java.util.Optional;

class DiskDriveCompositeStorage<T> extends AbstractImmutableConfiguredProxyStorage<T, CompositeStorageImpl<T>>
    implements CompositeStorage<T>, CompositeAwareChild<T> {
    protected DiskDriveCompositeStorage(final StorageConfiguration config) {
        super(config);
        this.delegate = new CompositeStorageImpl<>(new ResourceListImpl<>());
    }

    @Override
    public void sortSources() {
        // no-op: cannot sort individual disks.
    }

    private CompositeStorageImpl<T> delegate() {
        return Objects.requireNonNull(delegate);
    }

    @Override
    public void addSource(final Storage<T> source) {
        delegate().addSource(source);
    }

    @Override
    public void removeSource(final Storage<T> source) {
        delegate().removeSource(source);
    }

    @Override
    public void clearSources() {
        delegate().clearSources();
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final T resource,
                                                                    final Class<? extends Actor> actorType) {
        return delegate().findTrackedResourceByActorType(resource, actorType);
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite<T> parentComposite) {
        delegate().onAddedIntoComposite(parentComposite);
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite<T> parentComposite) {
        delegate().onRemovedFromComposite(parentComposite);
    }
}
