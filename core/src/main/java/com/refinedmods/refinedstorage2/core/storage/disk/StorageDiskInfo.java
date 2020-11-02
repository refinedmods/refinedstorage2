package com.refinedmods.refinedstorage2.core.storage.disk;

public class StorageDiskInfo {
    public static final StorageDiskInfo UNKNOWN = new StorageDiskInfo(0, 0);

    private final int capacity;
    private final int stored;

    public StorageDiskInfo(int capacity, int stored) {
        this.capacity = capacity;
        this.stored = stored;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getStored() {
        return stored;
    }
}
