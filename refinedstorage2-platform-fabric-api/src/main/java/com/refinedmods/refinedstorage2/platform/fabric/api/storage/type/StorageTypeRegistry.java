package com.refinedmods.refinedstorage2.platform.fabric.api.storage.type;

import java.util.Optional;

import net.minecraft.util.Identifier;

public interface StorageTypeRegistry {
    StorageTypeRegistry INSTANCE = new StorageTypeRegistryImpl();

    void addType(Identifier identifier, StorageType<?> type);

    Optional<StorageType<?>> getType(Identifier identifier);

    Optional<Identifier> getIdentifier(StorageType<?> type);
}
