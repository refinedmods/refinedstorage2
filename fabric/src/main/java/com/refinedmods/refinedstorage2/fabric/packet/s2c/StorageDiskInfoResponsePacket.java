package com.refinedmods.refinedstorage2.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.core.storage.disk.ClientStorageDiskManager;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class StorageDiskInfoResponsePacket implements PacketConsumer {
    public static final Identifier ID = new Identifier(RefinedStorage2Mod.ID, "storage_disk_info_response");

    @Override
    public void accept(PacketContext packetContext, PacketByteBuf packetByteBuf) {
        UUID id = packetByteBuf.readUuid();
        int stored = packetByteBuf.readInt();
        int capacity = packetByteBuf.readInt();

        packetContext.getTaskQueue().execute(() -> ((ClientStorageDiskManager) RefinedStorage2Mod.API.getStorageDiskManager(packetContext.getPlayer().getEntityWorld())).setInfo(id, stored, capacity));
    }
}
