package com.refinedmods.refinedstorage.platform.common.storage;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage.platform.api.storage.StorageType;

public final class StorageTypes {
    public static final StorageType ITEM = new ItemStorageType();
    public static final StorageType FLUID = new FluidStorageType();

    private StorageTypes() {
    }

    static <T extends ResourceKey> PlatformStorage createHomogeneousStorage(final StorageType type,
                                                                            final StorageCodecs.StorageData<T> data,
                                                                            final Runnable listener) {
        final TrackedStorageRepository trackingRepository = new InMemoryTrackedStorageRepository();
        final TrackedStorageImpl tracked = new TrackedStorageImpl(
            new InMemoryStorageImpl(),
            trackingRepository,
            System::currentTimeMillis
        );
        final PlatformStorage storage = data.capacity().map(capacity -> {
            final LimitedStorageImpl limited = new LimitedStorageImpl(tracked, capacity);
            return (PlatformStorage) new LimitedPlatformStorage(limited, type, trackingRepository, listener);
        }).orElseGet(() -> new PlatformStorage(tracked, type, trackingRepository, listener));
        data.resources().forEach(storage::load);
        return storage;
    }
}
