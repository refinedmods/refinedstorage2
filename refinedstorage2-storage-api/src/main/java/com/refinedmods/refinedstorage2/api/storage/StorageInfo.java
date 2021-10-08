package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorage;

public record StorageInfo(long stored, long capacity) {
    public static final StorageInfo UNKNOWN = new StorageInfo(0, 0);

    public static StorageInfo of(Storage<?> storage) {
        return new StorageInfo(
                storage.getStored(),
                storage instanceof BulkStorage bulkStorage ? bulkStorage.getCapacity() : 0L
        );
    }
}
