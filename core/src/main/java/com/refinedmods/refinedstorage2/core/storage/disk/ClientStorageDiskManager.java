package com.refinedmods.refinedstorage2.core.storage.disk;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class ClientStorageDiskManager implements StorageDiskManager {
    private final Map<UUID, StorageDiskInfo> info = new HashMap<>();
    private final Consumer<UUID> requestInfoCallback;

    public ClientStorageDiskManager(Consumer<UUID> requestInfoCallback) {
        this.requestInfoCallback = requestInfoCallback;
    }

    @Override
    public <T> Optional<StorageDisk<T>> getDisk(UUID id) {
        throw new IllegalStateException("Server-side only");
    }

    @Override
    public <T> void setDisk(UUID id, StorageDisk<T> storage) {
        throw new IllegalStateException("Server-side only");
    }

    public void setInfo(UUID id, int stored, int capacity) {
        info.put(id, new StorageDiskInfo(stored, capacity));
    }

    @Override
    public StorageDiskInfo getInfo(UUID id) {
        requestInfoCallback.accept(id);
        return info.getOrDefault(id, StorageDiskInfo.UNKNOWN);
    }
}
