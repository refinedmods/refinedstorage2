package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class GridInsertPacket {
    private final boolean single;

    public GridInsertPacket(boolean single) {
        this.single = single;
    }

    public static GridInsertPacket decode(FriendlyByteBuf buf) {
        return new GridInsertPacket(buf.readBoolean());
    }

    public static void encode(GridInsertPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.single);
    }

    public static void handle(GridInsertPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(GridInsertPacket packet, Player player) {
        AbstractContainerMenu menu = player.containerMenu;
        GridInsertMode mode = packet.single ? GridInsertMode.SINGLE_RESOURCE : GridInsertMode.ENTIRE_RESOURCE;
        if (menu instanceof ItemGridEventHandler itemGridEventHandler) {
            itemGridEventHandler.onInsert(mode);
        } else if (menu instanceof FluidGridEventHandler fluidGridEventHandler) {
            fluidGridEventHandler.onInsert(mode);
        }
    }
}
