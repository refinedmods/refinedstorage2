package com.refinedmods.refinedstorage2.platform.fabric.api.storage.type;

import java.util.Optional;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;

// TODO: Add test
public class StorageTypeRegistryImpl implements StorageTypeRegistry {
    private final BiMap<ResourceLocation, StorageType<?>> types = HashBiMap.create();

    @Override
    public void addType(ResourceLocation identifier, StorageType<?> type) {
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
