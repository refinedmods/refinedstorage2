package com.refinedmods.refinedstorage2.api.storage.disk;

import java.util.Optional;
import java.util.UUID;

public interface StorageDiskManager {
    <T> Optional<StorageDisk<T>> getDisk(UUID id);

    <T> void setDisk(UUID id, StorageDisk<T> disk);

    <T> Optional<StorageDisk<T>> disassembleDisk(UUID id);

    StorageDiskInfo getInfo(UUID id);
}
