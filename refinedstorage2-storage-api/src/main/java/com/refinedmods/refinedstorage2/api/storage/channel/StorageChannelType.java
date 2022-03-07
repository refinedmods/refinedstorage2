package com.refinedmods.refinedstorage2.api.storage.channel;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface StorageChannelType<T> {
    StorageChannel<T> create();
}
