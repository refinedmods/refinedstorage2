package com.refinedmods.refinedstorage.platform.common.support.packet.s2c;

import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.platform.common.wirelesstransmitter.WirelessTransmitterContainerMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public record WirelessTransmitterRangePacket(int range) implements CustomPacketPayload {
    public static final Type<WirelessTransmitterRangePacket> PACKET_TYPE = new Type<>(
        createIdentifier("wireless_transmitter_range")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, WirelessTransmitterRangePacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, WirelessTransmitterRangePacket::range,
            WirelessTransmitterRangePacket::new
        );

    public static void handle(final WirelessTransmitterRangePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof WirelessTransmitterContainerMenu containerMenu) {
            containerMenu.setRange(packet.range);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
