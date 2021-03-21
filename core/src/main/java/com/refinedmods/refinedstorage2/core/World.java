package com.refinedmods.refinedstorage2.core;

import java.util.Optional;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public interface World {
    Optional<BlockEntity> getBlockEntity(BlockPos pos);

    boolean isPowered(BlockPos pos);
}
