package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class StorageInfoRequestPacket {
    private final UUID id;

    public StorageInfoRequestPacket(final UUID id) {
        this.id = id;
    }

    public static StorageInfoRequestPacket decode(final FriendlyByteBuf buf) {
        return new StorageInfoRequestPacket(buf.readUUID());
    }

    public static void encode(final StorageInfoRequestPacket packet, final FriendlyByteBuf buf) {
        buf.writeUUID(packet.id);
    }

    public static void handle(final StorageInfoRequestPacket packet, final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final StorageInfoRequestPacket packet, final ServerPlayer player) {
        final StorageInfo info = PlatformApi.INSTANCE
            .getStorageRepository(player.getCommandSenderWorld())
            .getInfo(packet.id);

        Platform.INSTANCE.getServerToClientCommunications().sendStorageInfoResponse(player, packet.id, info);
    }
}
