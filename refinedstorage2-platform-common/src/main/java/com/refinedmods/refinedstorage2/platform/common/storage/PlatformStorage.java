package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
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

class PlatformStorage extends AbstractProxyStorage implements SerializableStorage, TrackedStorage {
    private final StorageType type;
    private final TrackedStorageRepository trackingRepository;
    private final Runnable listener;

    PlatformStorage(final Storage delegate,
                    final StorageType type,
                    final TrackedStorageRepository trackingRepository,
                    final Runnable listener) {
        super(delegate);
        this.type = type;
        this.trackingRepository = trackingRepository;
        this.listener = listener;
    }

    void load(final ResourceKey resource, final long amount, @Nullable final String changedBy, final long changedAt) {
        if (!type.isAllowed(resource)) {
            return;
        }
        super.insert(resource, amount, Action.EXECUTE, EmptyActor.INSTANCE);
        if (changedBy != null && !changedBy.isBlank()) {
            trackingRepository.update(resource, new PlayerActor(changedBy), changedAt);
        }
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        if (!type.isAllowed(resource)) {
            return 0;
        }
        final long extracted = super.extract(resource, amount, action, actor);
        if (extracted > 0 && action == Action.EXECUTE) {
            listener.run();
        }
        return extracted;
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        if (!type.isAllowed(resource)) {
            return 0;
        }
        final long inserted = super.insert(resource, amount, action, actor);
        if (inserted > 0 && action == Action.EXECUTE) {
            listener.run();
        }
        return inserted;
    }

    @Override
    public StorageType getType() {
        return type;
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final ResourceKey resource,
                                                                    final Class<? extends Actor> actorType) {
        return trackingRepository.findTrackedResourceByActorType(resource, actorType);
    }
}
