package com.refinedmods.refinedstorage2.platform.fabric.block;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid.ItemGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.init.BlockColorMap;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class FluidGridBlock extends GridBlock {
    public FluidGridBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected BlockColorMap<?> getBlockColorMap() {
        return Rs2Mod.BLOCKS.getFluidGrid();
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ItemGridBlockEntity(pos, state);
    }
}
