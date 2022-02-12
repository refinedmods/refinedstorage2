package com.refinedmods.refinedstorage2.platform.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class LevelUtil {
    private LevelUtil() {
    }

    public static void updateBlock(Level level, BlockPos pos, BlockState state) {
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
    }
}
