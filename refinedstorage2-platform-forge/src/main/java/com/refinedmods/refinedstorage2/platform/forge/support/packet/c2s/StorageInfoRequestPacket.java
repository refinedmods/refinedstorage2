package com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record StorageInfoRequestPacket(UUID storageId) implements CustomPacketPayload {
    public static StorageInfoRequestPacket decode(final FriendlyByteBuf buf) {
        return new StorageInfoRequestPacket(buf.readUUID());
    }

    public static void handle(final StorageInfoRequestPacket packet, final PlayPayloadContext context) {
        context.player().ifPresent(player -> context.workHandler().submitAsync(() -> {
            final StorageInfo info = PlatformApi.INSTANCE
                .getStorageRepository(player.getCommandSenderWorld())
                .getInfo(packet.storageId());
            Platform.INSTANCE.getServerToClientCommunications().sendStorageInfoResponse(
                (ServerPlayer) player,
                packet.storageId(),
                info
            );
        }));
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeUUID(storageId);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.STORAGE_INFO_REQUEST;
    }
}
