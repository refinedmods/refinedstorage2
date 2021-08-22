package com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk;

import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskManager;

public interface PlatformStorageDiskManager extends StorageDiskManager {
    void markAsChanged();
}
