package com.refinedmods.refinedstorage2.api.storage;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public record StorageInfo(long stored, long capacity) {
    public static final StorageInfo UNKNOWN = new StorageInfo(0, 0);

    public static StorageInfo of(Storage<?> storage) {
        return new StorageInfo(
                storage.getStored(),
                storage instanceof CapacityAccessor capacityAccessor ? capacityAccessor.getCapacity() : 0L
        );
    }
}
