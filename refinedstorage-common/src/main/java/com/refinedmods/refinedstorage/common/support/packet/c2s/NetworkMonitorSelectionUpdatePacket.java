package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.common.networking.NetworkMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record NetworkMonitorSelectionUpdatePacket(Optional<UUID> deviceId) implements CustomPacketPayload {
    public static final Type<NetworkMonitorSelectionUpdatePacket> PACKET_TYPE = new Type<>(
        createIdentifier("network_monitor_selection_update")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorSelectionUpdatePacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), NetworkMonitorSelectionUpdatePacket::deviceId,
            NetworkMonitorSelectionUpdatePacket::new
        );

    public static void handle(final NetworkMonitorSelectionUpdatePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof NetworkMonitorContainerMenu menu) {
            menu.updateServerSelection(packet.deviceId.orElse(null));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
