package com.refinedmods.refinedstorage2.api.storage.channel;

import java.util.Set;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface StorageChannelTypeRegistry {
    StorageChannelTypeRegistry INSTANCE = new StorageChannelTypeRegistryImpl();

    void addType(StorageChannelType<?> type);

    Set<StorageChannelType<?>> getTypes();
}
