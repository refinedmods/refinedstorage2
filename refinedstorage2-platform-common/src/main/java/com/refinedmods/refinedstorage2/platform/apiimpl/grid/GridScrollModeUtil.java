package com.refinedmods.refinedstorage2.platform.apiimpl.grid;

import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;

import net.minecraft.network.FriendlyByteBuf;

public final class GridScrollModeUtil {
    private GridScrollModeUtil() {
    }

    public static GridScrollMode getMode(final byte mode) {
        if (mode == 0) {
            return GridScrollMode.GRID_TO_INVENTORY;
        } else if (mode == 1) {
            return GridScrollMode.GRID_TO_CURSOR;
        }
        return GridScrollMode.INVENTORY_TO_GRID;
    }

    public static void writeMode(final FriendlyByteBuf buf, final GridScrollMode mode) {
        switch (mode) {
            case GRID_TO_INVENTORY -> buf.writeByte(0);
            case GRID_TO_CURSOR -> buf.writeByte(1);
            case INVENTORY_TO_GRID -> buf.writeByte(2);
        }
    }
}
