package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategy;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class GridInsertPacket {
    private final boolean single;
    private final boolean tryAlternatives;

    public GridInsertPacket(final boolean single, final boolean tryAlternatives) {
        this.single = single;
        this.tryAlternatives = tryAlternatives;
    }

    public static GridInsertPacket decode(final FriendlyByteBuf buf) {
        return new GridInsertPacket(buf.readBoolean(), buf.readBoolean());
    }

    public static void encode(final GridInsertPacket packet, final FriendlyByteBuf buf) {
        buf.writeBoolean(packet.single);
        buf.writeBoolean(packet.tryAlternatives);
    }

    public static void handle(final GridInsertPacket packet, final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final GridInsertPacket packet, final Player player) {
        if (player.containerMenu instanceof GridInsertionStrategy strategy) {
            final GridInsertMode mode = packet.single ? GridInsertMode.SINGLE_RESOURCE : GridInsertMode.ENTIRE_RESOURCE;
            strategy.onInsert(mode, packet.tryAlternatives);
        }
    }
}
