package com.refinedmods.refinedstorage.common.api.storage;

import com.refinedmods.refinedstorage.api.storage.Storage;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface SerializableStorage extends Storage {
    StorageType getType();

    StorageContents toContents();
}
