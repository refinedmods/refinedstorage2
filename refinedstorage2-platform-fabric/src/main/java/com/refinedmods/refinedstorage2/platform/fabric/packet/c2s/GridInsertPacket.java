package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.fluid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item.ItemGridEventHandler;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class GridInsertPacket implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        boolean single = buf.readBoolean();

        server.execute(() -> {
            AbstractContainerMenu screenHandler = player.containerMenu;
            GridInsertMode mode = single ? GridInsertMode.SINGLE_RESOURCE : GridInsertMode.ENTIRE_RESOURCE;
            if (screenHandler instanceof ItemGridEventHandler itemGridEventHandler) {
                itemGridEventHandler.onInsert(mode);
            } else if (screenHandler instanceof FluidGridEventHandler fluidGridEventHandler) {
                fluidGridEventHandler.onInsert(mode);
            }
        });
    }
}
