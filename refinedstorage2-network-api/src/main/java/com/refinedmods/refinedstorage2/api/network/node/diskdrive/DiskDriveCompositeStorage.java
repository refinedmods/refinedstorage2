package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.Source;
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

    protected DiskDriveCompositeStorage(DiskDriveNetworkNode diskDrive, Filter filter) {
        this.disks = new CompositeStorageImpl<>(new ResourceListImpl<>());
        this.diskDrive = diskDrive;
        this.filter = filter;
    }

    @Override
    public long extract(T resource, long amount, Action action, Source source) {
        if (diskDrive.getAccessMode() == AccessMode.INSERT || !diskDrive.isActive()) {
            return 0;
        }
        return disks.extract(resource, amount, action, source);
    }

    @Override
    public long insert(T resource, long amount, Action action, Source source) {
        if (diskDrive.getAccessMode() == AccessMode.EXTRACT || !diskDrive.isActive() || !filter.isAllowed(resource)) {
            return 0;
        }
        return disks.insert(resource, amount, action, source);
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
    public void addSource(Storage<T> source) {
        disks.addSource(source);
    }

    @Override
    public void removeSource(Storage<T> source) {
        disks.removeSource(source);
    }

    @Override
    public void clearSources() {
        disks.clearSources();
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceBySourceType(T resource, Class<? extends Source> sourceType) {
        return disks.findTrackedResourceBySourceType(resource, sourceType);
    }

    @Override
    public void onAddedIntoComposite(ParentComposite<T> parentComposite) {
        disks.onAddedIntoComposite(parentComposite);
    }

    @Override
    public void onRemovedFromComposite(ParentComposite<T> parentComposite) {
        disks.onRemovedFromComposite(parentComposite);
    }
}
