package com.refinedmods.refinedstorage2.core.storage.disk;

public record StorageDiskInfo(long stored, long capacity) {
    public static final StorageDiskInfo UNKNOWN = new StorageDiskInfo(0, 0);

    public long getStored() {
        return stored;
    }

    public long getCapacity() {
        return capacity;
    }
}
