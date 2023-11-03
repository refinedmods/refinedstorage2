package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageType;

import java.util.Optional;
import javax.annotation.Nullable;

class PlatformStorage<T> extends AbstractProxyStorage<T> implements SerializableStorage<T>, TrackedStorage<T> {
    private final StorageType<T> type;
    private final TrackedStorageRepository<T> trackingRepository;
    private final Runnable listener;

    PlatformStorage(final Storage<T> delegate,
                    final StorageType<T> type,
                    final TrackedStorageRepository<T> trackingRepository,
                    final Runnable listener) {
        super(delegate);
        this.type = type;
        this.trackingRepository = trackingRepository;
        this.listener = listener;
    }

    void load(final T resource, final long amount, @Nullable final String changedBy, final long changedAt) {
        super.insert(resource, amount, Action.EXECUTE, EmptyActor.INSTANCE);
        if (changedBy != null && !changedBy.isBlank()) {
            trackingRepository.update(resource, new PlayerActor(changedBy), changedAt);
        }
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        final long extracted = super.extract(resource, amount, action, actor);
        if (extracted > 0 && action == Action.EXECUTE) {
            listener.run();
        }
        return extracted;
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        final long inserted = super.insert(resource, amount, action, actor);
        if (inserted > 0 && action == Action.EXECUTE) {
            listener.run();
        }
        return inserted;
    }

    @Override
    public StorageType<T> getType() {
        return type;
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final T resource,
                                                                    final Class<? extends Actor> actorType) {
        return trackingRepository.findTrackedResourceByActorType(resource, actorType);
    }
}
