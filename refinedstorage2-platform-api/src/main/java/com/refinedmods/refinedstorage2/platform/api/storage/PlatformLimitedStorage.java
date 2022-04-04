package com.refinedmods.refinedstorage2.platform.api.storage;

import com.refinedmods.refinedstorage2.api.storage.CapacityAccessor;
import com.refinedmods.refinedstorage2.api.storage.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;

public class PlatformLimitedStorage<T> extends PlatformStorage<T> implements CapacityAccessor {
    private final LimitedStorageImpl<T> limitedStorage;

    public PlatformLimitedStorage(LimitedStorageImpl<T> parent, StorageType<T> type, TrackedStorageRepository<T> trackingRepository, Runnable listener) {
        super(parent, type, trackingRepository, listener);
        this.limitedStorage = parent;
    }

    @Override
    public long getCapacity() {
        return limitedStorage.getCapacity();
    }
}
