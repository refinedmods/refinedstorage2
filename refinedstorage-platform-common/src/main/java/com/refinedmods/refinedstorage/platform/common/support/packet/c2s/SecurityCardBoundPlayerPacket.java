package com.refinedmods.refinedstorage.platform.common.support.packet.c2s;

import com.refinedmods.refinedstorage.platform.common.security.SecurityCardContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public record SecurityCardBoundPlayerPacket(UUID playerId) implements CustomPacketPayload {
    public static final Type<SecurityCardBoundPlayerPacket> PACKET_TYPE = new Type<>(
        createIdentifier("security_card_bound_player")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SecurityCardBoundPlayerPacket> STREAM_CODEC = StreamCodec
        .composite(
            UUIDUtil.STREAM_CODEC, SecurityCardBoundPlayerPacket::playerId,
            SecurityCardBoundPlayerPacket::new
        );

    public static void handle(final SecurityCardBoundPlayerPacket packet, final PacketContext ctx) {
        final Player player = ctx.getPlayer();
        final MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        if (player.containerMenu instanceof SecurityCardContainerMenu securityCardContainerMenu) {
            securityCardContainerMenu.setBoundPlayer(server, packet.playerId);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
