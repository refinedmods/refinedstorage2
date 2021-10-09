package com.refinedmods.refinedstorage2.platform.fabric.api.storage.bulk;

import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageType;

public interface StorageTypeAccessor<T> {
    StorageType<T> getType();
}
