package com.refinedmods.refinedstorage2.api.storage;

import java.util.Optional;
import java.util.UUID;

public interface StorageManager {
    <T> Optional<Storage<T>> get(UUID id);

    <T> void set(UUID id, Storage<T> storage);

    <T> Optional<Storage<T>> disassemble(UUID id);

    StorageInfo getInfo(UUID id);
}
