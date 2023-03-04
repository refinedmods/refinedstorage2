package com.refinedmods.refinedstorage2.platform.common.block;

import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.block.CableBlockSupport.DOWN;
import static com.refinedmods.refinedstorage2.platform.common.block.CableBlockSupport.EAST;
import static com.refinedmods.refinedstorage2.platform.common.block.CableBlockSupport.NORTH;
import static com.refinedmods.refinedstorage2.platform.common.block.CableBlockSupport.SOUTH;
import static com.refinedmods.refinedstorage2.platform.common.block.CableBlockSupport.UP;
import static com.refinedmods.refinedstorage2.platform.common.block.CableBlockSupport.WEST;

public record CableShapeCacheKey(boolean north, boolean east, boolean south, boolean west, boolean up, boolean down) {
    static CableShapeCacheKey of(final BlockState state) {
        return new CableShapeCacheKey(
            state.getValue(NORTH),
            state.getValue(EAST),
            state.getValue(SOUTH),
            state.getValue(WEST),
            state.getValue(UP),
            state.getValue(DOWN)
        );
    }
}
