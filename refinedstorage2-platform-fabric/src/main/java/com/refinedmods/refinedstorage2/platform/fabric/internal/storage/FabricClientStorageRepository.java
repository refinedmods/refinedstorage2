package com.refinedmods.refinedstorage2.platform.fabric.internal.storage;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.abstractions.PlatformAbstractions;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FabricClientStorageRepository implements PlatformStorageRepository {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<UUID, StorageInfo> info = new HashMap<>();
    private final RateLimiter rateLimiter = RateLimiter.create(2);

    @Override
    public <T> Optional<Storage<T>> get(UUID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void set(UUID id, Storage<T> storage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<Storage<T>> disassemble(UUID id) {
        throw new UnsupportedOperationException();
    }

    public void setInfo(UUID id, long stored, long capacity) {
        info.put(id, new StorageInfo(stored, capacity));
    }

    @Override
    public StorageInfo getInfo(UUID id) {
        trySendRequestPacket(id);
        return info.getOrDefault(id, StorageInfo.UNKNOWN);
    }

    private void trySendRequestPacket(UUID id) {
        if (!rateLimiter.tryAcquire()) {
            return;
        }
        LOGGER.debug("Sending request info packet for {}", id);
        PlatformAbstractions.INSTANCE.getClientToServerCommunications().sendStorageInfoRequest(id);
    }

    @Override
    public void markAsChanged() {
        throw new UnsupportedOperationException();
    }
}
