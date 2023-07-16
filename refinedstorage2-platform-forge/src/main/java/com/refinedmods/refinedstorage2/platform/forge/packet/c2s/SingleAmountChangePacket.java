package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractSingleAmountContainerMenu;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class SingleAmountChangePacket {
    private final double amount;

    public SingleAmountChangePacket(final double amount) {
        this.amount = amount;
    }

    public static SingleAmountChangePacket decode(final FriendlyByteBuf buf) {
        return new SingleAmountChangePacket(buf.readDouble());
    }

    public static void encode(final SingleAmountChangePacket packet, final FriendlyByteBuf buf) {
        buf.writeDouble(packet.amount);
    }

    public static void handle(final SingleAmountChangePacket packet, final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final SingleAmountChangePacket packet, final Player player) {
        if (player.containerMenu instanceof AbstractSingleAmountContainerMenu singleAmountContainerMenu) {
            singleAmountContainerMenu.changeAmountOnServer(packet.amount);
        }
    }
}
