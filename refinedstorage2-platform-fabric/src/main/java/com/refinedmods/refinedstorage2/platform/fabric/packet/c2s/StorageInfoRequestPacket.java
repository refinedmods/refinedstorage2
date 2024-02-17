package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class StorageInfoRequestPacket implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(final MinecraftServer server,
                        final ServerPlayer player,
                        final ServerGamePacketListenerImpl handler,
                        final FriendlyByteBuf buf,
                        final PacketSender responseSender) {
        final UUID id = buf.readUUID();

        server.execute(() -> {
            final StorageInfo info = PlatformApi.INSTANCE
                .getStorageRepository(player.getCommandSenderWorld())
                .getInfo(id);

            Platform.INSTANCE.getServerToClientCommunications().sendStorageInfoResponse(player, id, info);
        });
    }
}
