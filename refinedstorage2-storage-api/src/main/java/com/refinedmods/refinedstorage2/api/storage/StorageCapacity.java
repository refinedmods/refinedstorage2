package com.refinedmods.refinedstorage2.api.storage;

import org.apiguardian.api.API;

/**
 * Implement this on storages that can have a capacity.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface StorageCapacity {
    /**
     * @return the capacity
     */
    long getCapacity();
}
