package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;

public class StateTrackedStorage<T> implements TrackedStorage<T> {
    private static final double NEAR_CAPACITY_THRESHOLD = .75;

    private final Storage<T> delegate;
    @Nullable
    private final Listener listener;
    private StorageState state;

    public StateTrackedStorage(final Storage<T> delegate, @Nullable final Listener listener) {
        this.delegate = delegate;
        this.listener = listener;
        this.state = computeState();
    }

    public StorageState getState() {
        return state;
    }

    private StorageState computeState() {
        if (delegate instanceof LimitedStorage<T> limitedStorage) {
            return computeState(limitedStorage.getCapacity(), delegate.getStored());
        }
        return StorageState.NORMAL;
    }

    private StorageState computeState(final long capacity, final long stored) {
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
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        final long extracted = delegate.extract(resource, amount, action, actor);
        // TODO: https://sonarcloud.io/component_measures?metric=new_coverage&selected=refinedmods_refinedstorage2%3
        //  Arefinedstorage2-storage-api%2Fsrc%2Fmain%2Fjava%2Fcom%2Frefinedmods%2Frefinedstorage2%2Fapi%2
        //  Fstorage%2FStateTrackedStorage.java&view=list&pullRequest=465&id=refinedmods_refinedstorage2
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

    public static <T> TypedStorage<T, StateTrackedStorage<T>> of(
        final TypedStorage<T, Storage<T>> delegate,
        @Nullable final Listener listener
    ) {
        return new TypedStorage<>(
            new StateTrackedStorage<>(delegate.storage(), listener),
            delegate.storageChannelType()
        );
    }

    @FunctionalInterface
    public interface Listener {
        void onStorageStateChanged();
    }
}
