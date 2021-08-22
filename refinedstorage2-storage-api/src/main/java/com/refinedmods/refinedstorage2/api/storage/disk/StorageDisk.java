package com.refinedmods.refinedstorage2.api.storage.disk;

import com.refinedmods.refinedstorage2.api.storage.Storage;

public interface StorageDisk<T> extends Storage<T> {
    long getCapacity();
}
