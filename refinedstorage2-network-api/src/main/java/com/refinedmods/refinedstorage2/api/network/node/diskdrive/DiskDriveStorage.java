package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorageListener;
import com.refinedmods.refinedstorage2.api.storage.composite.Priority;

import java.util.Collection;
import java.util.Collections;

public class DiskDriveStorage<T> implements CompositeStorage<T>, Priority {
    private final CompositeStorage<T> compositeOfDisks;
    private final DiskDriveNetworkNode diskDrive;
    private final Filter filter;

    protected DiskDriveStorage(DiskDriveNetworkNode diskDrive, Filter filter) {
        this.compositeOfDisks = new CompositeStorageImpl<>(new ResourceListImpl<>());
        this.diskDrive = diskDrive;
        this.filter = filter;
    }

    @Override
    public long extract(T resource, long amount, Action action) {
        if (diskDrive.getAccessMode() == AccessMode.INSERT || !diskDrive.isActive()) {
            return 0;
        }
        return compositeOfDisks.extract(resource, amount, action);
    }

    @Override
    public long insert(T resource, long amount, Action action) {
        if (diskDrive.getAccessMode() == AccessMode.EXTRACT || !diskDrive.isActive() || !filter.isAllowed(resource)) {
            return amount;
        }
        return compositeOfDisks.insert(resource, amount, action);
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        if (!diskDrive.isActive()) {
            return Collections.emptyList();
        }
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
}
