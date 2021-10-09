package com.refinedmods.refinedstorage2.api.storage.disk;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;

public class StorageDiskManagerImpl implements StorageDiskManager {
    private final Map<UUID, StorageDisk<?>> disks = new HashMap<>();

    public Set<Map.Entry<UUID, StorageDisk<?>>> getDisks() {
        return disks.entrySet();
    }

    @Override
    public <T> Optional<StorageDisk<T>> getDisk(UUID id) {
        return Optional.ofNullable((StorageDisk<T>) disks.get(id));
    }

    @Override
    public <T> void setDisk(UUID id, StorageDisk<T> disk) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(disk);

        if (disks.containsKey(id)) {
            throw new IllegalArgumentException(id + " already exists");
        }

        disks.put(id, disk);
    }

    @Override
    public <T> Optional<StorageDisk<T>> disassembleDisk(UUID id) {
        return getDisk(id)
                .map(disk -> {
                    if (disk.getStored() == 0) {
                        disks.remove(id);
                        return (StorageDisk<T>) disk;
                    }
                    return null;
                });
    }

    @Override
    public StorageInfo getInfo(UUID id) {
        return getDisk(id).map(StorageInfo::of).orElse(StorageInfo.UNKNOWN);
    }
}
