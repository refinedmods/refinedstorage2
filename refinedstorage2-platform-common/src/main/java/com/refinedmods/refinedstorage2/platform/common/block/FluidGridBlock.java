package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.FluidGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FluidGridBlock extends GridBlock {
    private final MutableComponent name;

    public FluidGridBlock(MutableComponent name) {
        super(BlockConstants.STONE_PROPERTIES);
        this.name = name;
    }

    @Override
    public MutableComponent getName() {
        return name;
    }

    @Override
    protected BlockColorMap<?> getBlockColorMap() {
        return Blocks.INSTANCE.getFluidGrid();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidGridBlockEntity(pos, state);
    }
}
