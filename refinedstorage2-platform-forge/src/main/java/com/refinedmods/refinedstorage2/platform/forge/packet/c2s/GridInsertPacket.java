package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.ItemGridEventHandler;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class GridInsertPacket {
    private final boolean single;

    public GridInsertPacket(final boolean single) {
        this.single = single;
    }

    public static GridInsertPacket decode(final FriendlyByteBuf buf) {
        return new GridInsertPacket(buf.readBoolean());
    }

    public static void encode(final GridInsertPacket packet, final FriendlyByteBuf buf) {
        buf.writeBoolean(packet.single);
    }

    public static void handle(final GridInsertPacket packet, final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final GridInsertPacket packet, final Player player) {
        final AbstractContainerMenu menu = player.containerMenu;
        final GridInsertMode mode = packet.single ? GridInsertMode.SINGLE_RESOURCE : GridInsertMode.ENTIRE_RESOURCE;
        if (menu instanceof ItemGridEventHandler itemGridEventHandler) {
            itemGridEventHandler.onInsert(mode);
        } else if (menu instanceof FluidGridEventHandler fluidGridEventHandler) {
            fluidGridEventHandler.onInsert(mode);
        }
    }
}
