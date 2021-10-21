package com.refinedmods.refinedstorage2.api.storage.channel;

import java.util.HashSet;
import java.util.Set;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class StorageChannelTypeRegistryImpl implements StorageChannelTypeRegistry {
    private final Set<StorageChannelType<?>> types = new HashSet<>();

    @Override
    public void addType(StorageChannelType<?> type) {
        types.add(type);
    }

    @Override
    public Set<StorageChannelType<?>> getTypes() {
        return types;
    }
}
