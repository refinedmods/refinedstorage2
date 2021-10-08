package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;

import java.util.Optional;
import java.util.UUID;

public interface StorageManager {
    <T> Optional<StorageDisk<T>> get(UUID id);

    <T> void set(UUID id, StorageDisk<T> disk);

    <T> Optional<StorageDisk<T>> disassemble(UUID id);

    StorageInfo getInfo(UUID id);
}
