package com.refinedmods.refinedstorage2.platform.api.storage;

import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;

public interface StorageTypeAccessor<T> {
    StorageType<T> getType();
}
