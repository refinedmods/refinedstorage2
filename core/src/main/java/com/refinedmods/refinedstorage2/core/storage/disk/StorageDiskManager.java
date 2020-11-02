package com.refinedmods.refinedstorage2.core.storage.disk;

import java.util.Optional;
import java.util.UUID;

public interface StorageDiskManager {
    <T> Optional<StorageDisk<T>> getDisk(UUID id);

    <T> void setDisk(UUID id, StorageDisk<T> storage);

    StorageDiskInfo getInfo(UUID id);
}
