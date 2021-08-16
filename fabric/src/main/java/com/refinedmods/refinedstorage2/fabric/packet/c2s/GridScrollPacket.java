package com.refinedmods.refinedstorage2.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class GridScrollPacket implements ServerPlayNetworking.PlayChannelHandler {
    public static void writeMode(PacketByteBuf buf, GridScrollMode mode) {
        switch (mode) {
            case GRID_TO_INVENTORY:
                buf.writeByte(0);
                break;
            case GRID_TO_CURSOR:
                buf.writeByte(1);
                break;
            case INVENTORY_TO_GRID:
                buf.writeByte(2);
                break;
        }
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        Rs2ItemStack stack = PacketUtil.readItemStack(buf, false);
        GridScrollMode mode = getMode(buf.readByte());
        int slot = buf.readInt();

        server.execute(() -> {
            ScreenHandler screenHandler = player.currentScreenHandler;
            if (screenHandler instanceof GridEventHandler gridEventHandler) {
                gridEventHandler.onScroll(stack, slot, mode);
            }
        });
    }

    private GridScrollMode getMode(byte mode) {
        if (mode == 0) {
            return GridScrollMode.GRID_TO_INVENTORY;
        } else if (mode == 1) {
            return GridScrollMode.GRID_TO_CURSOR;
        }
        return GridScrollMode.INVENTORY_TO_GRID;
    }
}
