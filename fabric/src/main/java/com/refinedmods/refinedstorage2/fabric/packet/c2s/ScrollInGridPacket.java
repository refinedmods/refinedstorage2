package com.refinedmods.refinedstorage2.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.ScrollInGridMode;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class ScrollInGridPacket implements PacketConsumer {
    public static final Identifier ID = new Identifier(RefinedStorage2Mod.ID, "scroll_in_grid");

    @Override
    public void accept(PacketContext packetContext, PacketByteBuf buf) {
        ItemStack stack = PacketUtil.readItemStackWithoutCount(buf);
        ScrollInGridMode mode = getMode(buf.readByte());

        packetContext.getTaskQueue().execute(() -> {
            ScreenHandler handler = packetContext.getPlayer().currentScreenHandler;
            if (handler instanceof GridEventHandler) {
                ((GridEventHandler) handler).onScrollInGrid(stack, mode);
            }
        });
    }

    private ScrollInGridMode getMode(byte mode) {
        if (mode == 0) {
            return ScrollInGridMode.EXTRACT_STACK_FROM_GRID;
        } else if (mode == 1) {
            return ScrollInGridMode.EXTRACT_SINGLE_STACK_FROM_GRID;
        } else if (mode == 2) {
            return ScrollInGridMode.EXTRACT_STACK_FROM_INVENTORY;
        }
        return ScrollInGridMode.EXTRACT_SINGLE_STACK_FROM_INVENTORY;
    }

    public static void writeMode(PacketByteBuf buf, ScrollInGridMode mode) {
        switch (mode) {
            case EXTRACT_STACK_FROM_GRID:
                buf.writeByte(0);
                break;
            case EXTRACT_SINGLE_STACK_FROM_GRID:
                buf.writeByte(1);
                break;
            case EXTRACT_STACK_FROM_INVENTORY:
                buf.writeByte(2);
                break;
            case EXTRACT_SINGLE_STACK_FROM_INVENTORY:
                buf.writeByte(3);
                break;
        }
    }
}
