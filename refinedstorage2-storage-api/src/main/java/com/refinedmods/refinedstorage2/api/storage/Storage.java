package com.refinedmods.refinedstorage2.api.storage;

import org.apiguardian.api.API;

/**
 * Represents a storage that can be inserted into, extracted and read from.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface Storage<T> extends StorageView<T>, InsertableStorage<T>, ExtractableStorage<T> {
}
