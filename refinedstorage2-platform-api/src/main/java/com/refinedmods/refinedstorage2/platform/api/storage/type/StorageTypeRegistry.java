package com.refinedmods.refinedstorage2.platform.api.storage.type;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public interface StorageTypeRegistry {
    void addType(ResourceLocation identifier, StorageType<?> type);

    Optional<StorageType<?>> getType(ResourceLocation identifier);

    Optional<ResourceLocation> getIdentifier(StorageType<?> type);
}
