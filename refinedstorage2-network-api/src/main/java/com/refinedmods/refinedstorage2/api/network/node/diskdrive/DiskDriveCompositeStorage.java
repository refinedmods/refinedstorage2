package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorageListener;
import com.refinedmods.refinedstorage2.api.storage.composite.Priority;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Collection;
import java.util.Optional;

public class DiskDriveCompositeStorage<T> implements CompositeStorage<T>, Priority {
    private final CompositeStorage<T> compositeOfDisks;
    private final DiskDriveNetworkNode diskDrive;
    private final Filter filter;

    protected DiskDriveCompositeStorage(DiskDriveNetworkNode diskDrive, Filter filter) {
        this.compositeOfDisks = new CompositeStorageImpl<>(new ResourceListImpl<>());
        this.diskDrive = diskDrive;
        this.filter = filter;
    }

    @Override
    public long extract(T resource, long amount, Action action, Source source) {
        if (diskDrive.getAccessMode() == AccessMode.INSERT || !diskDrive.isActive()) {
            return 0;
        }
        return compositeOfDisks.extract(resource, amount, action, source);
    }

    @Override
    public long insert(T resource, long amount, Action action, Source source) {
        if (diskDrive.getAccessMode() == AccessMode.EXTRACT || !diskDrive.isActive() || !filter.isAllowed(resource)) {
            return 0;
        }
        return compositeOfDisks.insert(resource, amount, action, source);
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return compositeOfDisks.getAll();
    }

    @Override
    public long getStored() {
        return compositeOfDisks.getStored();
    }

    @Override
    public int getPriority() {
        return diskDrive.getPriority();
    }

    @Override
    public void sortSources() {
        compositeOfDisks.sortSources();
    }

    @Override
    public void addSource(Storage<T> source) {
        compositeOfDisks.addSource(source);
    }

    @Override
    public void removeSource(Storage<T> source) {
        compositeOfDisks.removeSource(source);
    }

    @Override
    public void addListener(CompositeStorageListener<T> listener) {
        compositeOfDisks.addListener(listener);
    }

    @Override
    public void removeListener(CompositeStorageListener<T> listener) {
        compositeOfDisks.removeListener(listener);
    }

    @Override
    public void clearSources() {
        compositeOfDisks.clearSources();
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceBySourceType(T resource, Class<? extends Source> sourceType) {
        return compositeOfDisks.findTrackedResourceBySourceType(resource, sourceType);
    }
}
