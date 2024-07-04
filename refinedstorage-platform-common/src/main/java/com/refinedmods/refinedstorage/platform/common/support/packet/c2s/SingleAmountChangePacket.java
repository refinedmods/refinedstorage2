package com.refinedmods.refinedstorage.platform.common.support.packet.c2s;

import com.refinedmods.refinedstorage.platform.common.support.containermenu.AbstractSingleAmountContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public record SingleAmountChangePacket(double amount) implements CustomPacketPayload {
    public static final Type<SingleAmountChangePacket> PACKET_TYPE = new Type<>(
        createIdentifier("single_amount_change")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SingleAmountChangePacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.DOUBLE, SingleAmountChangePacket::amount,
            SingleAmountChangePacket::new
        );

    public static void handle(final SingleAmountChangePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractSingleAmountContainerMenu singleAmountContainerMenu) {
            singleAmountContainerMenu.changeAmountOnServer(packet.amount());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
