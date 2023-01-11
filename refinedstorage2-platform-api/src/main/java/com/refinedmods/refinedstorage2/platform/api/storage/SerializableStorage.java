package com.refinedmods.refinedstorage2.platform.api.storage;

import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface SerializableStorage<T> {
    StorageType<T> getType();
}
