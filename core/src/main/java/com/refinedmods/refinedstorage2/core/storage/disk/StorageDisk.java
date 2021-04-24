package com.refinedmods.refinedstorage2.core.storage.disk;

import com.refinedmods.refinedstorage2.core.storage.Storage;

public interface StorageDisk<T> extends Storage<T> {
    long getCapacity();
}
