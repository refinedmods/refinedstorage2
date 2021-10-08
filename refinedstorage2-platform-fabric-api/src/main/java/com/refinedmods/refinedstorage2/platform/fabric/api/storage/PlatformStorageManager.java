package com.refinedmods.refinedstorage2.platform.fabric.api.storage;

import com.refinedmods.refinedstorage2.api.storage.StorageManager;

public interface PlatformStorageManager extends StorageManager {
    void markAsChanged();
}
