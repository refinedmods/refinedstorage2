package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.containermenu.detector.DetectorContainerMenu;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class DetectorAmountChangePacket {
    private final long amount;

    public DetectorAmountChangePacket(final long amount) {
        this.amount = amount;
    }

    public static DetectorAmountChangePacket decode(final FriendlyByteBuf buf) {
        return new DetectorAmountChangePacket(buf.readLong());
    }

    public static void encode(final DetectorAmountChangePacket packet, final FriendlyByteBuf buf) {
        buf.writeLong(packet.amount);
    }

    public static void handle(final DetectorAmountChangePacket packet, final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final DetectorAmountChangePacket packet, final Player player) {
        if (player.containerMenu instanceof DetectorContainerMenu detectorContainerMenu) {
            detectorContainerMenu.changeAmountOnServer(packet.amount);
        }
    }
}
