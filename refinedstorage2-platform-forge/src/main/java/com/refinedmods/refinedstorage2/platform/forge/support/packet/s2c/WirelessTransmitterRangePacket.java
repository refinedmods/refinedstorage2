package com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.common.wirelesstransmitter.WirelessTransmitterContainerMenu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record WirelessTransmitterRangePacket(int range) implements CustomPacketPayload {
    public static WirelessTransmitterRangePacket decode(final FriendlyByteBuf buf) {
        return new WirelessTransmitterRangePacket(buf.readInt());
    }

    public static void handle(final WirelessTransmitterRangePacket packet, final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (player.containerMenu instanceof WirelessTransmitterContainerMenu containerMenu) {
                containerMenu.setRange(packet.range);
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(range);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.WIRELESS_TRANSMITTER_RANGE;
    }
}
