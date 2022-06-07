
package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class StorageInfoRequestPacket {
    private final UUID id;

    public StorageInfoRequestPacket(UUID id) {
        this.id = id;
    }

    public static StorageInfoRequestPacket decode(FriendlyByteBuf buf) {
        return new StorageInfoRequestPacket(buf.readUUID());
    }

    public static void encode(StorageInfoRequestPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.id);
    }

    public static void handle(StorageInfoRequestPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(StorageInfoRequestPacket packet, ServerPlayer player) {
        StorageInfo info = PlatformApi.INSTANCE
                .getStorageRepository(player.getCommandSenderWorld())
                .getInfo(packet.id);

        Platform.INSTANCE.getServerToClientCommunications().sendStorageInfoResponse(player, packet.id, info);
    }
}
