package com.refinedmods.refinedstorage2.fabric.api.storage.disk;

import com.refinedmods.refinedstorage2.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.fabric.util.ClientPacketUtil;

import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FabricRequestInfoCallback implements Consumer<UUID> {
    private static final Logger LOGGER = LogManager.getLogger(FabricRequestInfoCallback.class);

    private final RateLimiter rateLimiter = RateLimiter.create(2);

    @Override
    public void accept(UUID id) {
        if (!rateLimiter.tryAcquire()) {
            return;
        }

        LOGGER.debug("Sending request info packet for {}", id);

        ClientPacketUtil.sendToServer(PacketIds.STORAGE_DISK_INFO_REQUEST, data -> data.writeUuid(id));
    }
}
