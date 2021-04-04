package com.refinedmods.refinedstorage2.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class GridScrollPacket implements ServerPlayNetworking.PlayChannelHandler {
    public static final Identifier ID = new Identifier(RefinedStorage2Mod.ID, "grid_scroll");

    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ItemStack stack = PacketUtil.readItemStackWithoutCount(buf);
        GridScrollMode mode = getMode(buf.readByte());
        int slot = buf.readInt();

        server.execute(() -> {
            ScreenHandler screenHandler = player.currentScreenHandler;
            if (screenHandler instanceof GridEventHandler) {
                ((GridEventHandler) screenHandler).onScroll(stack, slot, mode);
            }
        });
    }

    private GridScrollMode getMode(byte mode) {
        if (mode == 0) {
            return GridScrollMode.GRID_TO_INVENTORY_STACK;
        } else if (mode == 1) {
            return GridScrollMode.GRID_TO_INVENTORY_SINGLE_STACK;
        } else if (mode == 2) {
            return GridScrollMode.INVENTORY_TO_GRID_STACK;
        }
        return GridScrollMode.INVENTORY_TO_GRID_SINGLE_STACK;
    }

    public static void writeMode(PacketByteBuf buf, GridScrollMode mode) {
        switch (mode) {
            case GRID_TO_INVENTORY_STACK:
                buf.writeByte(0);
                break;
            case GRID_TO_INVENTORY_SINGLE_STACK:
                buf.writeByte(1);
                break;
            case INVENTORY_TO_GRID_STACK:
                buf.writeByte(2);
                break;
            case INVENTORY_TO_GRID_SINGLE_STACK:
                buf.writeByte(3);
                break;
        }
    }
}
