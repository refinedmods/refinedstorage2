package com.refinedmods.refinedstorage2.core.graph;

import com.refinedmods.refinedstorage2.core.World;
import net.minecraft.util.math.BlockPos;

public class BlockEntityRequest {
    private final World world;
    private final BlockPos pos;

    public BlockEntityRequest(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public World getWorldAdapter() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }
}
