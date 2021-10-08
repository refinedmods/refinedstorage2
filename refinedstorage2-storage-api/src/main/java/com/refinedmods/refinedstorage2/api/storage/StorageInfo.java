package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;

public record StorageInfo(long stored, long capacity) {
    public static final StorageInfo UNKNOWN = new StorageInfo(0, 0);

    public static StorageInfo of(Storage<?> storage) {
        return new StorageInfo(
                storage.getStored(),
                storage instanceof StorageDisk storageDisk ? storageDisk.getCapacity() : 0L
        );
    }
}
