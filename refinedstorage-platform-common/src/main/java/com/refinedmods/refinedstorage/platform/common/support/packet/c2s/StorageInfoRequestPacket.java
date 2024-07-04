package com.refinedmods.refinedstorage.platform.common.support.packet.c2s;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.storage.StorageInfo;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.S2CPackets;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public record StorageInfoRequestPacket(UUID storageId) implements CustomPacketPayload {
    public static final Type<StorageInfoRequestPacket> PACKET_TYPE = new Type<>(
        createIdentifier("storage_info_request")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, StorageInfoRequestPacket> STREAM_CODEC = StreamCodec
        .composite(
            UUIDUtil.STREAM_CODEC, StorageInfoRequestPacket::storageId,
            StorageInfoRequestPacket::new
        );

    public static void handle(final StorageInfoRequestPacket packet, final PacketContext ctx) {
        final Player player = ctx.getPlayer();
        final StorageInfo info = PlatformApi.INSTANCE
            .getStorageRepository(player.getCommandSenderWorld())
            .getInfo(packet.storageId());
        S2CPackets.sendStorageInfoResponse((ServerPlayer) player, packet.storageId, info);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
