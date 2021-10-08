package com.refinedmods.refinedstorage2.platform.fabric.api.storage.bulk;

import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorage;

public interface PlatformBulkStorage<T> extends BulkStorage<T> {
    StorageDiskType<T> getType();
}
