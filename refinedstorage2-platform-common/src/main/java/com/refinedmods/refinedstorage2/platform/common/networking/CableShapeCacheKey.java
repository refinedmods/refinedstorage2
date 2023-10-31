package com.refinedmods.refinedstorage2.platform.common.networking;

import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.support.CableBlockSupport.DOWN;
import static com.refinedmods.refinedstorage2.platform.common.support.CableBlockSupport.EAST;
import static com.refinedmods.refinedstorage2.platform.common.support.CableBlockSupport.NORTH;
import static com.refinedmods.refinedstorage2.platform.common.support.CableBlockSupport.SOUTH;
import static com.refinedmods.refinedstorage2.platform.common.support.CableBlockSupport.UP;
import static com.refinedmods.refinedstorage2.platform.common.support.CableBlockSupport.WEST;

public record CableShapeCacheKey(boolean north, boolean east, boolean south, boolean west, boolean up, boolean down) {
    public static CableShapeCacheKey of(final BlockState state) {
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
