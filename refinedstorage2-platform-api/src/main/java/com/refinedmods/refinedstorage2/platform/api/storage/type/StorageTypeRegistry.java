package com.refinedmods.refinedstorage2.platform.api.storage.type;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

public interface StorageTypeRegistry {
    StorageTypeRegistry INSTANCE = new StorageTypeRegistryImpl();

    void addType(ResourceLocation identifier, StorageType<?> type);

    Optional<StorageType<?>> getType(ResourceLocation identifier);

    Optional<ResourceLocation> getIdentifier(StorageType<?> type);
}
