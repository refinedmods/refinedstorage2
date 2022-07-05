package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public class StorageRepositoryImpl implements StorageRepository {
    private final Map<UUID, Storage<?>> entries = new HashMap<>();

    public Set<Map.Entry<UUID, Storage<?>>> getAll() {
        return entries.entrySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Storage<T>> get(final UUID id) {
        return Optional.ofNullable((Storage<T>) entries.get(id));
    }

    @Override
    public <T> void set(final UUID id, final Storage<T> storage) {
        CoreValidations.validateNotNull(id, "ID must not be null");
        CoreValidations.validateNotNull(storage, "Storage must not be null");
        if (entries.containsKey(id)) {
            throw new IllegalArgumentException(id + " already exists");
        }
        entries.put(id, storage);
    }

    @Override
    public <T> Optional<Storage<T>> disassemble(final UUID id) {
        return this.<T>get(id).map(storage -> {
            if (storage.getStored() == 0) {
                entries.remove(id);
                return storage;
            }
            return null;
        });
    }

    @Override
    public StorageInfo getInfo(final UUID id) {
        return get(id).map(StorageInfo::of).orElse(StorageInfo.UNKNOWN);
    }
}
