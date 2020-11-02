package com.refinedmods.refinedstorage2.core.storage.disk;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class StorageDiskManagerImpl implements StorageDiskManager {
    private final Map<UUID, StorageDisk<?>> storages = new HashMap<>();

    @Override
    public <T> Optional<StorageDisk<T>> getDisk(UUID id) {
        return Optional.ofNullable((StorageDisk<T>) storages.get(id));
    }

    @Override
    public <T> void setDisk(UUID id, StorageDisk<T> storage) {
        storages.put(id, storage);
    }

    @Override
    public StorageDiskInfo getInfo(UUID id) {
        return getDisk(id)
            .map(disk -> new StorageDiskInfo(disk.getCapacity(), disk.getStored()))
            .orElse(StorageDiskInfo.UNKNOWN);
    }
}
