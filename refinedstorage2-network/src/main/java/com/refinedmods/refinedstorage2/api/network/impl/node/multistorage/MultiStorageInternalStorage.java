package com.refinedmods.refinedstorage2.api.network.impl.node.multistorage;

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

public class MultiStorageInternalStorage<T> implements TrackedStorage<T> {
    private static final double NEAR_CAPACITY_THRESHOLD = .75;

    private final Storage<T> delegate;
    private final StorageChannelType<T> storageChannelType;
    @Nullable
    private final MultiStorageListener listener;
    private MultiStorageStorageState state;

    public MultiStorageInternalStorage(final Storage<T> delegate,
                                       final StorageChannelType<T> storageChannelType,
                                       @Nullable final MultiStorageListener listener) {
        this.delegate = delegate;
        this.storageChannelType = storageChannelType;
        this.listener = listener;
        this.state = computeState();
    }

    public StorageChannelType<T> getStorageChannelType() {
        return storageChannelType;
    }

    public MultiStorageStorageState computeState() {
        if (delegate instanceof LimitedStorage<?> limitedStorage) {
            return computeState(limitedStorage.getCapacity());
        }
        return MultiStorageStorageState.NORMAL;
    }

    private MultiStorageStorageState computeState(final long capacity) {
        final double fullness = (double) delegate.getStored() / capacity;
        if (fullness >= 1D) {
            return MultiStorageStorageState.FULL;
        } else if (fullness >= NEAR_CAPACITY_THRESHOLD) {
            return MultiStorageStorageState.NEAR_CAPACITY;
        } else {
            return MultiStorageStorageState.NORMAL;
        }
    }

    private void checkStateChanged() {
        final MultiStorageStorageState currentState = computeState();
        if (state != currentState) {
            this.state = currentState;
            notifyListener();
        }
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onStorageChanged();
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
    public Optional<TrackedResource> findTrackedResourceByActorType(final T resource,
                                                                    final Class<? extends Actor> actorType) {
        return delegate instanceof TrackedStorage<T> trackedStorage
            ? trackedStorage.findTrackedResourceByActorType(resource, actorType)
            : Optional.empty();
    }
}
