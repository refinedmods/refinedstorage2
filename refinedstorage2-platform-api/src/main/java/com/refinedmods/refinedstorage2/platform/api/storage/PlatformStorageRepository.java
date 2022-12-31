package com.refinedmods.refinedstorage2.platform.api.storage;

import com.refinedmods.refinedstorage2.api.storage.StorageRepository;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface PlatformStorageRepository extends StorageRepository {
    void markAsChanged();
}
