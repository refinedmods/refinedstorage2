package com.refinedmods.refinedstorage2.platform.apiimpl.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerSource;
import com.refinedmods.refinedstorage2.platform.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;

import java.util.Optional;
import javax.annotation.Nullable;

public class PlatformStorage<T> extends AbstractProxyStorage<T> implements SerializableStorage<T>, TrackedStorage<T> {
    private final StorageType<T> type;
    private final TrackedStorageRepository<T> trackingRepository;
    private final Runnable listener;

    public PlatformStorage(final Storage<T> delegate,
                           final StorageType<T> type,
                           final TrackedStorageRepository<T> trackingRepository,
                           final Runnable listener) {
        super(delegate);
        this.type = type;
        this.trackingRepository = trackingRepository;
        this.listener = listener;
    }

    public void load(final T resource, final long amount, @Nullable final String changedBy, final long changedAt) {
        super.insert(resource, amount, Action.EXECUTE, EmptySource.INSTANCE);
        if (changedBy != null && !changedBy.isBlank()) {
            trackingRepository.update(resource, new PlayerSource(changedBy), changedAt);
        }
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Source source) {
        final long extracted = super.extract(resource, amount, action, source);
        if (extracted > 0 && action == Action.EXECUTE) {
            listener.run();
        }
        return extracted;
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Source source) {
        final long inserted = super.insert(resource, amount, action, source);
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
    public Optional<TrackedResource> findTrackedResourceBySourceType(final T resource,
                                                                     final Class<? extends Source> sourceType) {
        return trackingRepository.findTrackedResourceBySourceType(resource, sourceType);
    }
}
