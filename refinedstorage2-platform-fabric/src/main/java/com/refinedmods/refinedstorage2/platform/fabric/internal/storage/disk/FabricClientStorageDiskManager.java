package com.refinedmods.refinedstorage2.platform.fabric.internal.storage.disk;

import com.google.common.util.concurrent.RateLimiter;

import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskInfo;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformStorageDiskManager;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.util.ClientPacketUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FabricClientStorageDiskManager implements PlatformStorageDiskManager {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<UUID, StorageDiskInfo> info = new HashMap<>();
    private final RateLimiter rateLimiter = RateLimiter.create(2);

    @Override
    public <T> Optional<StorageDisk<T>> getDisk(UUID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void setDisk(UUID id, StorageDisk<T> disk) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<StorageDisk<T>> disassembleDisk(UUID id) {
        throw new UnsupportedOperationException();
    }

    public void setInfo(UUID id, long stored, long capacity) {
        info.put(id, new StorageDiskInfo(stored, capacity));
    }

    @Override
    public StorageDiskInfo getInfo(UUID id) {
        trySendRequestPacket(id);
        return info.getOrDefault(id, StorageDiskInfo.UNKNOWN);
    }

    private void trySendRequestPacket(UUID id) {
        if (!rateLimiter.tryAcquire()) {
            return;
        }
        LOGGER.debug("Sending request info packet for {}", id);
        ClientPacketUtil.sendToServer(PacketIds.STORAGE_DISK_INFO_REQUEST, data -> data.writeUuid(id));
    }

    @Override
    public void markAsChanged() {
        throw new UnsupportedOperationException();
    }
}
