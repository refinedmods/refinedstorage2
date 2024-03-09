package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;

public class StateTrackedStorage implements TrackedStorage {
    private static final double NEAR_CAPACITY_THRESHOLD = .75;

    private final Storage delegate;
    @Nullable
    private final Listener listener;
    private StorageState state;

    public StateTrackedStorage(final Storage delegate, @Nullable final Listener listener) {
        this.delegate = delegate;
        this.listener = listener;
        this.state = computeState();
    }

    public StorageState getState() {
        return state;
    }

    private StorageState computeState() {
        if (delegate instanceof LimitedStorage limitedStorage) {
            return computeState(limitedStorage.getCapacity(), delegate.getStored());
        }
        return StorageState.NORMAL;
    }

    public static StorageState computeState(final long capacity, final long stored) {
        final double fullness = stored / (double) capacity;
        if (fullness >= 1D) {
            return StorageState.FULL;
        } else if (fullness >= NEAR_CAPACITY_THRESHOLD) {
            return StorageState.NEAR_CAPACITY;
        }
        return StorageState.NORMAL;
    }

    private void checkStateChanged() {
        final StorageState currentState = computeState();
        if (state != currentState) {
            this.state = currentState;
            notifyListener();
        }
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onStorageStateChanged();
        }
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        final long extracted = delegate.extract(resource, amount, action, actor);
        if (extracted > 0 && action == Action.EXECUTE) {
            checkStateChanged();
        }
        return extracted;
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        final long inserted = delegate.insert(resource, amount, action, actor);
        if (inserted > 0 && action == Action.EXECUTE) {
            checkStateChanged();
        }
        return inserted;
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        return delegate.getAll();
    }

    @Override
    public long getStored() {
        return delegate.getStored();
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final ResourceKey resource,
                                                                    final Class<? extends Actor> actorType) {
        return delegate instanceof TrackedStorage trackedStorage
            ? trackedStorage.findTrackedResourceByActorType(resource, actorType)
            : Optional.empty();
    }

    @FunctionalInterface
    public interface Listener {
        void onStorageStateChanged();
    }
}
