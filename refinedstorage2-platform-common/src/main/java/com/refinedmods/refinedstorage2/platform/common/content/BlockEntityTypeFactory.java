package com.refinedmods.refinedstorage2.platform.common.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface BlockEntityTypeFactory {
    <T extends BlockEntity> BlockEntityType<T> create(BlockEntitySupplier<T> factory, Block... allowedBlocks);

    @FunctionalInterface
    interface BlockEntitySupplier<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }
}
