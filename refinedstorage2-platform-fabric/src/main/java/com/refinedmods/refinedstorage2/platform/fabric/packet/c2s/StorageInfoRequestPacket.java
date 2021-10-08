package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.util.ServerPacketUtil;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class StorageInfoRequestPacket implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        UUID id = buf.readUuid();

        server.execute(() -> {
            StorageInfo info = Rs2PlatformApiFacade.INSTANCE
                    .getStorageManager(player.getEntityWorld())
                    .getInfo(id);

            ServerPacketUtil.sendToPlayer(player, PacketIds.STORAGE_INFO_RESPONSE, bufToSend -> {
                bufToSend.writeUuid(id);
                bufToSend.writeLong(info.stored());
                bufToSend.writeLong(info.capacity());
            });
        });
    }
}
