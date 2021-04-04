package com.refinedmods.refinedstorage2.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.GridInsertMode;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class GridInsertFromCursorPacket implements ServerPlayNetworking.PlayChannelHandler {
    public static final Identifier ID = new Identifier(RefinedStorage2Mod.ID, "grid_insert_from_cursor");

    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        boolean single = buf.readBoolean();

        server.execute(() -> {
            ScreenHandler screenHandler = player.currentScreenHandler;
            if (screenHandler instanceof GridEventHandler) {
                ((GridEventHandler) screenHandler).onInsertFromCursor(single ? GridInsertMode.SINGLE : GridInsertMode.ENTIRE_STACK);
            }
        });
    }
}
