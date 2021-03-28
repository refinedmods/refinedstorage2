package com.refinedmods.refinedstorage2.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.core.grid.GridSortingDirection;
import com.refinedmods.refinedstorage2.core.grid.GridSortingType;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.fabric.block.entity.grid.GridSettings;
import com.refinedmods.refinedstorage2.fabric.screen.handler.grid.GridScreenHandler;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class GridChangeSettingPacket implements PacketConsumer {
    private static final byte SORTING_DIRECTION = 0;
    private static final byte SORTING_TYPE = 1;

    public static final Identifier ID = new Identifier(RefinedStorage2Mod.ID, "grid_setting_change");

    @Override
    public void accept(PacketContext packetContext, PacketByteBuf buf) {
        byte type = buf.readByte();
        int value = buf.readInt();

        packetContext.getTaskQueue().execute(() -> {
            ScreenHandler screenHandler = packetContext.getPlayer().currentScreenHandler;
            if (screenHandler instanceof GridScreenHandler) {
                GridBlockEntity grid = ((GridScreenHandler) screenHandler).getGrid();

                if (type == SORTING_DIRECTION) {
                    grid.setSortingDirection(GridSettings.getSortingDirection(value));
                } else if (type == SORTING_TYPE) {
                    grid.setSortingType(GridSettings.getSortingType(value));
                }
            }
        });
    }

    public static void writeSortingDirection(PacketByteBuf buf, GridSortingDirection sortingDirection) {
        buf.writeByte(SORTING_DIRECTION);
        buf.writeInt(GridSettings.getSortingDirection(sortingDirection));
    }

    public static void writeSortingType(PacketByteBuf buf, GridSortingType sortingType) {
        buf.writeByte(SORTING_TYPE);
        buf.writeInt(GridSettings.getSortingType(sortingType));
    }
}
