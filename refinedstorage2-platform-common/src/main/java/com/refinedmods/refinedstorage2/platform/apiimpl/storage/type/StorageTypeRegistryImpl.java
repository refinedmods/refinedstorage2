package com.refinedmods.refinedstorage2.platform.apiimpl.storage.type;

import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageTypeRegistry;

import java.util.Optional;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;

public class StorageTypeRegistryImpl implements StorageTypeRegistry {
    private final BiMap<ResourceLocation, StorageType<?>> types = HashBiMap.create();

    @Override
    public void addType(ResourceLocation identifier, StorageType<?> type) {
        if (types.containsKey(identifier)) {
            throw new IllegalArgumentException(identifier + " already exists");
        }
        types.put(identifier, type);
    }

    @Override
    public Optional<StorageType<?>> getType(ResourceLocation identifier) {
        return Optional.ofNullable(types.get(identifier));
    }

    @Override
    public Optional<ResourceLocation> getIdentifier(StorageType<?> type) {
        return Optional.ofNullable(types.inverse().get(type));
    }
}
