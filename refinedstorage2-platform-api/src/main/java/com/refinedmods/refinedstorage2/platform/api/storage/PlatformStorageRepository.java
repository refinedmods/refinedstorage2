package com.refinedmods.refinedstorage2.platform.api.storage;

import com.refinedmods.refinedstorage2.api.storage.StorageRepository;

public interface PlatformStorageRepository extends StorageRepository {
    void markAsChanged();
}