package com.refinedmods.refinedstorage2.api.storage.channel;

import java.util.HashSet;
import java.util.Set;

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
