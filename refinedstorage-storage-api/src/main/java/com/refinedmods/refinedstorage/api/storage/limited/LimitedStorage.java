package com.refinedmods.refinedstorage.api.storage.limited;

import com.refinedmods.refinedstorage.api.storage.Storage;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface LimitedStorage extends Storage {
    long getCapacity();
}
