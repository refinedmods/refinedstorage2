package com.refinedmods.refinedstorage2.fabric.util;

import com.refinedmods.refinedstorage2.core.util.Position;
import net.minecraft.util.math.BlockPos;

public final class Positions {
    private Positions() {
    }

    public static Position ofBlockPos(BlockPos pos) {
        return new Position(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos toBlockPos(Position pos) {
        return new BlockPos(pos.getX(), pos.getY(), pos.getZ());
    }
}
