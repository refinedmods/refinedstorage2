package com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk;

import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;

public interface PlatformStorageDisk<T> extends StorageDisk<T> {
    StorageDiskType<T> getType();
}
