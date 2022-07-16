package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeAwareChild;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.composite.ParentComposite;
import com.refinedmods.refinedstorage2.api.storage.composite.Priority;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Collection;
import java.util.Optional;

class DiskDriveCompositeStorage<T> implements CompositeStorage<T>, CompositeAwareChild<T>, Priority {
    private final CompositeStorageImpl<T> disks;
    private final DiskDriveNetworkNode diskDrive;
    private final Filter filter;

    protected DiskDriveCompositeStorage(final DiskDriveNetworkNode diskDrive, final Filter filter) {
        this.disks = new CompositeStorageImpl<>(new ResourceListImpl<>());
        this.diskDrive = diskDrive;
        this.filter = filter;
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        if (diskDrive.getAccessMode() == AccessMode.INSERT || !diskDrive.isActive()) {
            return 0;
        }
        return disks.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        if (diskDrive.getAccessMode() == AccessMode.EXTRACT || !diskDrive.isActive() || !filter.isAllowed(resource)) {
            return 0;
        }
        return disks.insert(resource, amount, action, actor);
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return disks.getAll();
    }

    @Override
    public long getStored() {
        return disks.getStored();
    }

    @Override
    public int getPriority() {
        return diskDrive.getPriority();
    }

    @Override
    public void sortSources() {
        disks.sortSources();
    }

    @Override
    public void addSource(final Storage<T> source) {
        disks.addSource(source);
    }

    @Override
    public void removeSource(final Storage<T> source) {
        disks.removeSource(source);
    }

    @Override
    public void clearSources() {
        disks.clearSources();
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final T resource,
                                                                    final Class<? extends Actor> actorType) {
        return disks.findTrackedResourceByActorType(resource, actorType);
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite<T> parentComposite) {
        disks.onAddedIntoComposite(parentComposite);
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite<T> parentComposite) {
        disks.onRemovedFromComposite(parentComposite);
    }
}
