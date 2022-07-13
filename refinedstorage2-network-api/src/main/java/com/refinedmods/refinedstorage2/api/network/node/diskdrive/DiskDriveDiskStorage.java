package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;

public class DiskDriveDiskStorage<T> implements TrackedStorage<T> {
    private static final double DISK_NEAR_CAPACITY_THRESHOLD = .75;

    private final Storage<T> delegate;
    private final StorageChannelType<T> storageChannelType;
    @Nullable
    private final DiskDriveListener listener;
    private StorageDiskState state;

    public DiskDriveDiskStorage(final Storage<T> delegate,
                                final StorageChannelType<T> storageChannelType,
                                @Nullable final DiskDriveListener listener) {
        this.delegate = delegate;
        this.storageChannelType = storageChannelType;
        this.listener = listener;
        this.state = getState();
    }

    public StorageChannelType<T> getStorageChannelType() {
        return storageChannelType;
    }

    public StorageDiskState getState() {
        if (delegate instanceof LimitedStorage<?> limitedStorage) {
            return getStateWithCapacity(limitedStorage.getCapacity());
        }
        return StorageDiskState.NORMAL;
    }

    private StorageDiskState getStateWithCapacity(final long capacity) {
        final double fullness = (double) delegate.getStored() / capacity;

        if (fullness >= 1D) {
            return StorageDiskState.FULL;
        } else if (fullness >= DISK_NEAR_CAPACITY_THRESHOLD) {
            return StorageDiskState.NEAR_CAPACITY;
        } else {
            return StorageDiskState.NORMAL;
        }
    }

    private void checkStateChanged() {
        final StorageDiskState currentDiskState = getState();
        if (state != currentDiskState) {
            this.state = currentDiskState;
            notifyListener();
        }
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onDiskChanged();
        }
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        final long extracted = delegate.extract(resource, amount, action, actor);
        if (extracted > 0 && action == Action.EXECUTE) {
            checkStateChanged();
        }
        return extracted;
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        final long inserted = delegate.insert(resource, amount, action, actor);
        if (inserted > 0 && action == Action.EXECUTE) {
            checkStateChanged();
        }
        return inserted;
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return delegate.getAll();
    }

    @Override
    public long getStored() {
        return delegate.getStored();
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceBySourceType(final T resource,
                                                                     final Class<? extends Actor> actorType) {
        return delegate instanceof TrackedStorage<T> trackedStorage
            ? trackedStorage.findTrackedResourceBySourceType(resource, actorType)
            : Optional.empty();
    }
}
