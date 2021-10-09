package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.fluid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item.ItemGridEventHandler;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class GridInsertPacket implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        boolean single = buf.readBoolean();

        server.execute(() -> {
            ScreenHandler screenHandler = player.currentScreenHandler;
            GridInsertMode mode = single ? GridInsertMode.SINGLE_RESOURCE : GridInsertMode.ENTIRE_RESOURCE;
            if (screenHandler instanceof ItemGridEventHandler itemGridEventHandler) {
                itemGridEventHandler.onInsert(mode);
            } else if (screenHandler instanceof FluidGridEventHandler fluidGridEventHandler) {
                fluidGridEventHandler.onInsert(mode);
            }
        });
    }
}
