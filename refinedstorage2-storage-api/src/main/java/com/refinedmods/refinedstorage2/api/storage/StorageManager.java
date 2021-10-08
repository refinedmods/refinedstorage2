package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorage;

import java.util.Optional;
import java.util.UUID;

public interface StorageManager {
    <T> Optional<BulkStorage<T>> get(UUID id);

    <T> void set(UUID id, BulkStorage<T> storage);

    <T> Optional<BulkStorage<T>> disassemble(UUID id);

    StorageInfo getInfo(UUID id);
}
