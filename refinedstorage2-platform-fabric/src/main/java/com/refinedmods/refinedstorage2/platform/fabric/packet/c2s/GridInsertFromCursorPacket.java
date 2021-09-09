package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.eventhandler.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.api.grid.eventhandler.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.eventhandler.ItemGridEventHandler;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class GridInsertFromCursorPacket implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        boolean single = buf.readBoolean();

        server.execute(() -> {
            ScreenHandler screenHandler = player.currentScreenHandler;

            GridInsertMode mode = single ? GridInsertMode.SINGLE : GridInsertMode.ENTIRE_STACK;
            if (screenHandler instanceof ItemGridEventHandler eventHandler) {
                eventHandler.onInsertFromCursor(mode);
            } else if (screenHandler instanceof FluidGridEventHandler eventHandler) {
                eventHandler.onInsertFromCursor(mode);
            }
        });
    }
}
