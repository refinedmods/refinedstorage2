package com.refinedmods.refinedstorage2.platform.fabric.block;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid.FluidGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.init.BlockColorMap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidGridBlock extends GridBlock {
    public FluidGridBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockColorMap<?> getBlockColorMap() {
        return Rs2Mod.BLOCKS.getFluidGrid();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidGridBlockEntity(pos, state);
    }
}
