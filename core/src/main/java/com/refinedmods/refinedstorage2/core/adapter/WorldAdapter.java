package com.refinedmods.refinedstorage2.core.adapter;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public interface WorldAdapter {
    Optional<BlockEntity> getBlockEntity(BlockPos pos);
}
