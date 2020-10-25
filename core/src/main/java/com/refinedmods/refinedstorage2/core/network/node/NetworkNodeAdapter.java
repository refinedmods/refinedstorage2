package com.refinedmods.refinedstorage2.core.network.node;

import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public interface NetworkNodeAdapter {
    Optional<NetworkNode> getNode(BlockPos pos);
}
