package com.refinedmods.refinedstorage.platform.common.storage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.EmptyActor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.platform.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.platform.api.storage.StorageType;

import java.util.Optional;

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

    void load(final StorageCodecs.StorageResource<? extends ResourceKey> storageResource) {
        final ResourceKey resource = storageResource.resource();
        if (!type.isAllowed(resource)) {
            return;
        }
        super.insert(resource, storageResource.amount(), Action.EXECUTE, EmptyActor.INSTANCE);
        storageResource.changed().ifPresent(
            changed -> trackingRepository.update(resource, new PlayerActor(changed.changedBy()), changed.changedAt())
        );
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
