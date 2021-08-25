package com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk;

import java.util.Optional;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.Identifier;

// TODO: Add test
public class StorageDiskTypeRegistryImpl implements StorageDiskTypeRegistry {
    private final BiMap<Identifier, StorageDiskType<?>> types = HashBiMap.create();

    @Override
    public void addType(Identifier identifier, StorageDiskType<?> type) {
        types.put(identifier, type);
    }

    @Override
    public Optional<StorageDiskType<?>> getType(Identifier identifier) {
        return Optional.ofNullable(types.get(identifier));
    }

    @Override
    public Optional<Identifier> getIdentifier(StorageDiskType<?> type) {
        return Optional.ofNullable(types.inverse().get(type));
    }
}
