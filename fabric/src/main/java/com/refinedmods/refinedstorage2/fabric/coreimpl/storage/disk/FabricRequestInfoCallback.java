package com.refinedmods.refinedstorage2.fabric.coreimpl.storage.disk;

import com.refinedmods.refinedstorage2.core.util.ThrottleHelper;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.StorageDiskInfoRequestPacket;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.function.Consumer;

public class FabricRequestInfoCallback implements Consumer<UUID> {
    private static final Logger LOGGER = LogManager.getLogger(FabricRequestInfoCallback.class);

    private final ThrottleHelper<UUID> throttleHelper = new ThrottleHelper<>(500);

    @Override
    public void accept(UUID id) {
        throttleHelper.throttle(id, () -> {
            LOGGER.debug("Sending request info packet for {}", id);

            PacketUtil.sendToServer(StorageDiskInfoRequestPacket.ID, data -> data.writeUuid(id));
        }, System.currentTimeMillis());
    }
}
