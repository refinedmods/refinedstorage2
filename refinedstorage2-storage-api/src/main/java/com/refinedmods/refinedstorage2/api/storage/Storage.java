package com.refinedmods.refinedstorage2.api.storage;

import org.apiguardian.api.API;

/**
 * Represents a storage that can be inserted into, extracted and read from.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface Storage extends StorageView, InsertableStorage, ExtractableStorage {
}
