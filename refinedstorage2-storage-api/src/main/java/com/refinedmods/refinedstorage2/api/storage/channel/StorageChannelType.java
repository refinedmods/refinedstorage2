package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface StorageChannelType<T> {
    StorageChannel<T> create();

    CompositeStorage<T> createCompositeStorage(List<Storage<T>> sources);
}
