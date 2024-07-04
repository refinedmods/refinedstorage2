package com.refinedmods.refinedstorage.platform.common.storage;

import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage.platform.api.storage.StorageType;

class LimitedPlatformStorage extends PlatformStorage implements LimitedStorage {
    private final LimitedStorageImpl limitedStorage;

    LimitedPlatformStorage(final LimitedStorageImpl delegate,
                           final StorageType type,
                           final TrackedStorageRepository trackingRepository,
                           final Runnable listener) {
        super(delegate, type, trackingRepository, listener);
        this.limitedStorage = delegate;
    }

    @Override
    public long getCapacity() {
        return limitedStorage.getCapacity();
    }
}
