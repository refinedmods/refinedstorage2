package com.refinedmods.refinedstorage2.core.network.node;

import net.minecraft.util.math.BlockPos;

public interface NetworkNodeAdapter {
    NetworkNodeReference getReference(BlockPos pos);
}
