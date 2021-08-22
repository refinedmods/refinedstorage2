package com.refinedmods.refinedstorage2.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskInfo;
import com.refinedmods.refinedstorage2.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.fabric.util.ServerPacketUtil;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class StorageDiskInfoRequestPacket implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        UUID id = buf.readUuid();

        server.execute(() -> {
            StorageDiskInfo info = Rs2PlatformApiFacade.INSTANCE
                    .getStorageDiskManager(player.getEntityWorld())
                    .getInfo(id);

            ServerPacketUtil.sendToPlayer(player, PacketIds.STORAGE_DISK_INFO_RESPONSE, bufToSend -> {
                bufToSend.writeUuid(id);
                bufToSend.writeLong(info.getStored());
                bufToSend.writeLong(info.getCapacity());
            });
        });
    }
}
