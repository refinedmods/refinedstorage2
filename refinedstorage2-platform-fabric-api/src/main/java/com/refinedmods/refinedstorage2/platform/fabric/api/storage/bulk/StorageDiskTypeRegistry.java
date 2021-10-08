package com.refinedmods.refinedstorage2.platform.fabric.api.storage.bulk;

import java.util.Optional;

import net.minecraft.util.Identifier;

public interface StorageDiskTypeRegistry {
    StorageDiskTypeRegistry INSTANCE = new StorageDiskTypeRegistryImpl();

    void addType(Identifier identifier, StorageDiskType<?> type);

    Optional<StorageDiskType<?>> getType(Identifier identifier);

    Optional<Identifier> getIdentifier(StorageDiskType<?> type);
}
