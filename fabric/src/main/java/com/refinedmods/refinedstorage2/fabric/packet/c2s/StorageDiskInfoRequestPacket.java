package com.refinedmods.refinedstorage2.fabric.packet.c2s;

import java.util.UUID;

import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskInfo;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.packet.s2c.StorageDiskInfoResponsePacket;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class StorageDiskInfoRequestPacket implements ServerPlayNetworking.PlayChannelHandler {
    public static final Identifier ID = new Identifier(RefinedStorage2Mod.ID, "storage_disk_info_request");

    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        UUID id = buf.readUuid();

        server.execute(() -> {
            StorageDiskInfo info = RefinedStorage2Mod.API
                .getStorageDiskManager(player.getEntityWorld())
                .getInfo(id);

            PacketUtil.sendToPlayer(player, StorageDiskInfoResponsePacket.ID, bufToSend -> {
                bufToSend.writeUuid(id);
                bufToSend.writeInt(info.getStored());
                bufToSend.writeInt(info.getCapacity());
            });
        });
    }
}
