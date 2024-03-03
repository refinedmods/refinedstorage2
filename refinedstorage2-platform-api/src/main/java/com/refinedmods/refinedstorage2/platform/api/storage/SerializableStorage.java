package com.refinedmods.refinedstorage2.platform.api.storage;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface SerializableStorage {
    StorageType getType();
}
