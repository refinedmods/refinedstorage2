package com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractSingleAmountContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record SingleAmountChangePacket(double amount) implements CustomPacketPayload {
    public static SingleAmountChangePacket decode(final FriendlyByteBuf buf) {
        return new SingleAmountChangePacket(buf.readDouble());
    }

    public static void handle(final SingleAmountChangePacket packet, final PlayPayloadContext context) {
        context.player().ifPresent(player -> context.workHandler().submitAsync(() -> {
            if (player.containerMenu instanceof AbstractSingleAmountContainerMenu singleAmountContainerMenu) {
                singleAmountContainerMenu.changeAmountOnServer(packet.amount());
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeDouble(amount);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.SINGLE_AMOUNT_CHANGE;
    }
}
