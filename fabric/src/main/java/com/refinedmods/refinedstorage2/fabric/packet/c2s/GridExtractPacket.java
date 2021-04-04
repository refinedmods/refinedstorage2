package com.refinedmods.refinedstorage2.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.GridExtractMode;
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

public class GridExtractPacket implements ServerPlayNetworking.PlayChannelHandler {
    public static final Identifier ID = RefinedStorage2Mod.createIdentifier("grid_extract");

    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ItemStack stack = PacketUtil.readItemStackWithoutCount(buf);
        GridExtractMode mode = getMode(buf.readByte());

        server.execute(() -> {
            ScreenHandler screenHandler = player.currentScreenHandler;
            if (screenHandler instanceof GridEventHandler) {
                ((GridEventHandler) screenHandler).onExtract(stack, mode);
            }
        });
    }

    private GridExtractMode getMode(byte mode) {
        if (mode == 0) {
            return GridExtractMode.CURSOR_HALF;
        } else if (mode == 1) {
            return GridExtractMode.CURSOR_STACK;
        } else if (mode == 2) {
            return GridExtractMode.PLAYER_INVENTORY_STACK;
        }
        return GridExtractMode.PLAYER_INVENTORY_STACK;
    }

    public static void writeMode(PacketByteBuf buf, GridExtractMode mode) {
        switch (mode) {
            case CURSOR_HALF:
                buf.writeByte(0);
                break;
            case CURSOR_STACK:
                buf.writeByte(1);
                break;
            case PLAYER_INVENTORY_STACK:
                buf.writeByte(2);
                break;
        }
    }
}
