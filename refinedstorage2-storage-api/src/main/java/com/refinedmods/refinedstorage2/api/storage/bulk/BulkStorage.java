package com.refinedmods.refinedstorage2.api.storage.bulk;

import com.refinedmods.refinedstorage2.api.storage.Storage;

import org.apiguardian.api.API;

/**
 * A bulk storage is a {@link Storage} which has a capacity.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface BulkStorage<T> extends Storage<T> {
    /**
     * @return the capacity of this bulk storage
     */
    long getCapacity();
}
