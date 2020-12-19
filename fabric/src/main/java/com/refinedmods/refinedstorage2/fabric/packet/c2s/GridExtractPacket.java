package com.refinedmods.refinedstorage2.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.GridExtractMode;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class GridExtractPacket implements PacketConsumer {
    public static final Identifier ID = new Identifier(RefinedStorage2Mod.ID, "grid_extract");

    @Override
    public void accept(PacketContext packetContext, PacketByteBuf buf) {
        ItemStack stack = PacketUtil.readItemStackWithoutCount(buf);
        GridExtractMode mode = getMode(buf.readByte());

        packetContext.getTaskQueue().execute(() -> {
            ScreenHandler handler = packetContext.getPlayer().currentScreenHandler;
            if (handler instanceof GridEventHandler) {
                ((GridEventHandler) handler).onExtract(stack, mode);
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
