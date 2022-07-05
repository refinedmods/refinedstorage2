package com.refinedmods.refinedstorage2.platform.common.util;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class LevelUtil {
    private LevelUtil() {
    }

    public static void updateBlock(@Nullable final Level level, final BlockPos pos, final BlockState state) {
        if (level == null) {
            return;
        }
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
    }
}
