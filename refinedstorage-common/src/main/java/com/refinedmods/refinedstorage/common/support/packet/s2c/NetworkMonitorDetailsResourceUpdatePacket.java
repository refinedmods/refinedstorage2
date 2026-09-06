package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.networking.NetworkMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record NetworkMonitorDetailsResourceUpdatePacket(PlatformResourceKey resource,
                                                       long change,
                                                       long stored,
                                                       long capacity)
    implements CustomPacketPayload {
    public static final Type<NetworkMonitorDetailsResourceUpdatePacket> PACKET_TYPE = new Type<>(
        createIdentifier("network_monitor_details_resource_update")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorDetailsResourceUpdatePacket> STREAM_CODEC =
        StreamCodec.composite(
            ResourceCodecs.STREAM_CODEC, NetworkMonitorDetailsResourceUpdatePacket::resource,
            ByteBufCodecs.LONG, NetworkMonitorDetailsResourceUpdatePacket::change,
            ByteBufCodecs.LONG, NetworkMonitorDetailsResourceUpdatePacket::stored,
            ByteBufCodecs.LONG, NetworkMonitorDetailsResourceUpdatePacket::capacity,
            NetworkMonitorDetailsResourceUpdatePacket::new
        );

    public static void handle(final NetworkMonitorDetailsResourceUpdatePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof NetworkMonitorContainerMenu networkMonitor) {
            networkMonitor.updateDetailsResource(packet.resource, packet.change, packet.stored, packet.capacity);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
