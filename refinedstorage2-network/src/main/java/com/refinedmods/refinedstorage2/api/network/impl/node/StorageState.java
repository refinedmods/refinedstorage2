package com.refinedmods.refinedstorage2.api.network.impl.node;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.3")
public enum StorageState {
    /**
     * There is no storage in the container.
     */
    NONE,
    /**
     * There is a storage present in the container, but the container is inactive.
     */
    INACTIVE,
    /**
     * The storage is active and has enough capacity to store more resources.
     */
    NORMAL,
    /**
     * The storage is active and has less than 25% capacity left.
     */
    NEAR_CAPACITY,
    /**
     * The storage is active and full.
     */
    FULL;

    private static final double NEAR_CAPACITY_THRESHOLD = .75;

    public static <T> StorageState compute(final Storage<T> storage) {
        if (storage instanceof LimitedStorage<T> limitedStorage) {
            return compute(limitedStorage.getCapacity(), storage.getStored());
        }
        return StorageState.NORMAL;
    }

    private static StorageState compute(final long capacity, final long stored) {
        final double fullness = stored / (double) capacity;
        if (fullness >= 1D) {
            return FULL;
        } else if (fullness >= NEAR_CAPACITY_THRESHOLD) {
            return NEAR_CAPACITY;
        }
        return NORMAL;
    }
}
