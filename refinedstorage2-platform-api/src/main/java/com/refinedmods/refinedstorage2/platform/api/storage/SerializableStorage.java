package com.refinedmods.refinedstorage2.platform.api.storage;

import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;

public interface SerializableStorage<T> {
    StorageType<T> getType();
}
