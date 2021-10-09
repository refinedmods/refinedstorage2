package com.refinedmods.refinedstorage2.api.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;

public class StorageManagerImpl implements StorageManager {
    private final Map<UUID, Storage<?>> entries = new HashMap<>();

    public Set<Map.Entry<UUID, Storage<?>>> getAll() {
        return entries.entrySet();
    }

    @Override
    public <T> Optional<Storage<T>> get(UUID id) {
        return Optional.ofNullable((Storage<T>) entries.get(id));
    }

    @Override
    public <T> void set(UUID id, Storage<T> storage) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(storage);

        if (entries.containsKey(id)) {
            throw new IllegalArgumentException(id + " already exists");
        }

        entries.put(id, storage);
    }

    @Override
    public <T> Optional<Storage<T>> disassemble(UUID id) {
        return get(id).map(storage -> {
            if (storage.getStored() == 0) {
                entries.remove(id);
                return (Storage<T>) storage;
            }
            return null;
        });
    }

    @Override
    public StorageInfo getInfo(UUID id) {
        return get(id).map(StorageInfo::of).orElse(StorageInfo.UNKNOWN);
    }
}
