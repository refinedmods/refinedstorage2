package com.refinedmods.refinedstorage2.api.storage.disk;

public record StorageDiskInfo(long stored, long capacity) {
    public static final StorageDiskInfo UNKNOWN = new StorageDiskInfo(0, 0);
}
