package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.CapacityAccessor;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;

import java.util.Collection;
import java.util.Optional;

public class DiskDriveDiskStorage<T> implements TrackedStorage<T> {
    private static final double DISK_NEAR_CAPACITY_THRESHOLD = .75;

    private final Storage<T> parent;
    private final StorageChannelType<T> storageChannelType;
    private final DiskDriveListener listener;
    private StorageDiskState state;

    public DiskDriveDiskStorage(Storage<T> parent, StorageChannelType<T> storageChannelType, DiskDriveListener listener) {
        this.parent = parent;
        this.storageChannelType = storageChannelType;
        this.listener = listener;
        this.state = getState();
    }

    public StorageChannelType<T> getStorageChannelType() {
        return storageChannelType;
    }

    public StorageDiskState getState() {
        if (parent instanceof CapacityAccessor capacityAccessor) {
            return getStateWithCapacity(capacityAccessor.getCapacity());
        }
        return StorageDiskState.NORMAL;
    }

    private StorageDiskState getStateWithCapacity(long capacity) {
        double fullness = (double) parent.getStored() / capacity;

        if (fullness >= 1D) {
            return StorageDiskState.FULL;
        } else if (fullness >= DISK_NEAR_CAPACITY_THRESHOLD) {
            return StorageDiskState.NEAR_CAPACITY;
        } else {
            return StorageDiskState.NORMAL;
        }
    }

    private void checkStateChanged() {
        StorageDiskState currentDiskState = getState();
        if (state != currentDiskState) {
            this.state = currentDiskState;
            this.listener.onDiskChanged();
        }
    }

    @Override
    public long extract(T resource, long amount, Action action, Source source) {
        long extracted = parent.extract(resource, amount, action, source);
        if (extracted > 0 && action == Action.EXECUTE) {
            checkStateChanged();
        }
        return extracted;
    }

    @Override
    public long insert(T resource, long amount, Action action, Source source) {
        long inserted = parent.insert(resource, amount, action, source);
        if (inserted > 0 && action == Action.EXECUTE) {
            checkStateChanged();
        }
        return inserted;
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return parent.getAll();
    }

    @Override
    public long getStored() {
        return parent.getStored();
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceBySourceType(T resource, Class<? extends Source> sourceType) {
        return parent instanceof TrackedStorage<T> trackedStorage ? trackedStorage.findTrackedResourceBySourceType(resource, sourceType) : Optional.empty();
    }
}
