package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.common.networking.NetworkMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.networking.NetworkMonitorNetworkStatistics;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record NetworkMonitorNetworkStatisticsUpdatePacket(NetworkMonitorNetworkStatistics networkStatistics)
    implements CustomPacketPayload {
    public static final Type<NetworkMonitorNetworkStatisticsUpdatePacket> PACKET_TYPE =
        new Type<>(createIdentifier("network_monitor_network_statistics_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorNetworkStatisticsUpdatePacket> STREAM_CODEC =
        StreamCodec.composite(
            NetworkMonitorNetworkStatistics.STREAM_CODEC,
            NetworkMonitorNetworkStatisticsUpdatePacket::networkStatistics,
            NetworkMonitorNetworkStatisticsUpdatePacket::new
        );

    public static void handle(final NetworkMonitorNetworkStatisticsUpdatePacket packet, final PacketContext ctx) {
        final AbstractContainerMenu menu = ctx.getPlayer().containerMenu;
        if (menu instanceof NetworkMonitorContainerMenu networkMonitor) {
            networkMonitor.onNetworkStatisticsUpdated(packet.networkStatistics);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
