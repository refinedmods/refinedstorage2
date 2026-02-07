package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.api.storage.StorageContents;
import com.refinedmods.refinedstorage.common.api.storage.StorageType;

import java.util.List;
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

    void load(final StorageContents.Stored stored) {
        final ResourceKey resource = stored.resource();
        if (!type.isAllowed(resource)) {
            return;
        }
        super.insert(resource, stored.amount(), Action.EXECUTE, Actor.EMPTY);
        stored.changed().ifPresent(
            changed -> trackingRepository.update(resource, new PlayerActor(changed.by()), changed.at())
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
    public Optional<TrackedResource> findTrackedResourceByActorType(final ResourceKey resource,
                                                                    final Class<? extends Actor> actorType) {
        return trackingRepository.findTrackedResourceByActorType(resource, actorType);
    }

    @Override
    public StorageType getType() {
        return type;
    }

    @Override
    public StorageContents toContents() {
        final Optional<Long> capacity = this instanceof LimitedStorage limitedStorage
            ? Optional.of(limitedStorage.getCapacity())
            : Optional.empty();
        final List<StorageContents.Stored> stored = getAll().stream()
            .map(storedResource -> new StorageContents.Stored(storedResource.resource(), storedResource.amount(),
                toChanged(storedResource.resource())))
            .toList();
        return new StorageContents(type, capacity, stored);
    }

    private Optional<StorageContents.Changed> toChanged(final ResourceKey resourceAmount) {
        return findTrackedResourceByActorType(resourceAmount, PlayerActor.class)
            .map(tracked -> new StorageContents.Changed(tracked.getSourceName(), tracked.getTime()));
    }
}
