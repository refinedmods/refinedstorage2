package com.refinedmods.refinedstorage2.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskInfo;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.packet.s2c.StorageDiskInfoResponsePacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class StorageDiskInfoRequestPacket implements PacketConsumer {
    public static final Identifier ID = new Identifier(RefinedStorage2Mod.ID, "storage_disk_info_request");

    @Override
    public void accept(PacketContext packetContext, PacketByteBuf packetByteBuf) {
        UUID id = packetByteBuf.readUuid();

        packetContext.getTaskQueue().execute(() -> {
            StorageDiskInfo info = RefinedStorage2Mod.API
                .getStorageDiskManager(packetContext.getPlayer().getEntityWorld())
                .getInfo(id);

            PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
            data.writeUuid(id);
            data.writeInt(info.getStored());
            data.writeInt(info.getCapacity());

            ServerSidePacketRegistry.INSTANCE.sendToPlayer(packetContext.getPlayer(), StorageDiskInfoResponsePacket.ID, data);
        });
    }
}
