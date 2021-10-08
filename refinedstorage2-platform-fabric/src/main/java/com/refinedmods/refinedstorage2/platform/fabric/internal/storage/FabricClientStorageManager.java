package com.refinedmods.refinedstorage2.platform.fabric.internal.storage;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorage;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageManager;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.util.ClientPacketUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FabricClientStorageManager implements PlatformStorageManager {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<UUID, StorageInfo> info = new HashMap<>();
    private final RateLimiter rateLimiter = RateLimiter.create(2);

    @Override
    public <T> Optional<BulkStorage<T>> get(UUID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void set(UUID id, BulkStorage<T> storage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<BulkStorage<T>> disassemble(UUID id) {
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
        ClientPacketUtil.sendToServer(PacketIds.STORAGE_INFO_REQUEST, data -> data.writeUuid(id));
    }

    @Override
    public void markAsChanged() {
        throw new UnsupportedOperationException();
    }
}
