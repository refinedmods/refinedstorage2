package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.storage.Storage;

import org.apiguardian.api.API;

/**
 * A consuming storage is a storage that won't cause changes to be propagated in the {@link CompositeStorage} cache.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface ConsumingStorage<T> extends Storage<T> {
}
