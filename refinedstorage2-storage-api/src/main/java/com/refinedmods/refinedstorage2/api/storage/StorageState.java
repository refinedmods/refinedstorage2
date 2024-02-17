package com.refinedmods.refinedstorage2.api.storage;

import org.apiguardian.api.API;

/**
 * The storage state can be cached and obtained by {@link StateTrackedStorage}.
 */
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
    FULL
}
