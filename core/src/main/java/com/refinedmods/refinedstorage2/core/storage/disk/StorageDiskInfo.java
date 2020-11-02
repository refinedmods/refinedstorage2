package com.refinedmods.refinedstorage2.core.storage.disk;

public class StorageDiskInfo {
    public static final StorageDiskInfo UNKNOWN = new StorageDiskInfo(0, 0);

    private final int stored;
    private final int capacity;

    public StorageDiskInfo(int stored, int capacity) {
        this.stored = stored;
        this.capacity = capacity;
    }

    public int getStored() {
        return stored;
    }

    public int getCapacity() {
        return capacity;
    }
}
