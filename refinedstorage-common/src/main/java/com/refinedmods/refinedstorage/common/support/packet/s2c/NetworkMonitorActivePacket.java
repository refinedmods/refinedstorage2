package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.common.networking.NetworkMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record NetworkMonitorActivePacket(boolean active) implements CustomPacketPayload {
    public static final Type<NetworkMonitorActivePacket> PACKET_TYPE =
        new Type<>(createIdentifier("network_monitor_active"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorActivePacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, NetworkMonitorActivePacket::active,
            NetworkMonitorActivePacket::new
        );

    public static void handle(final NetworkMonitorActivePacket packet, final PacketContext ctx) {
        final AbstractContainerMenu menu = ctx.getPlayer().containerMenu;
        if (menu instanceof NetworkMonitorContainerMenu networkMonitor) {
            networkMonitor.onActiveChanged(packet.active);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
