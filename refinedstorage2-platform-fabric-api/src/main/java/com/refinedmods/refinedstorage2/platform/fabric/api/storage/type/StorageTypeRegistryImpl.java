package com.refinedmods.refinedstorage2.platform.fabric.api.storage.type;

import java.util.Optional;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.Identifier;

// TODO: Add test
public class StorageTypeRegistryImpl implements StorageTypeRegistry {
    private final BiMap<Identifier, StorageType<?>> types = HashBiMap.create();

    @Override
    public void addType(Identifier identifier, StorageType<?> type) {
        types.put(identifier, type);
    }

    @Override
    public Optional<StorageType<?>> getType(Identifier identifier) {
        return Optional.ofNullable(types.get(identifier));
    }

    @Override
    public Optional<Identifier> getIdentifier(StorageType<?> type) {
        return Optional.ofNullable(types.inverse().get(type));
    }
}
