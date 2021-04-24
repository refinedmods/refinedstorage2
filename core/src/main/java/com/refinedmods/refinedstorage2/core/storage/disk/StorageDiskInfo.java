package com.refinedmods.refinedstorage2.core.storage.disk;

public class StorageDiskInfo {
    public static final StorageDiskInfo UNKNOWN = new StorageDiskInfo(0, 0);

    private final long stored;
    private final long capacity;

    public StorageDiskInfo(long stored, long capacity) {
        this.stored = stored;
        this.capacity = capacity;
    }

    public long getStored() {
        return stored;
    }

    public long getCapacity() {
        return capacity;
    }
}
