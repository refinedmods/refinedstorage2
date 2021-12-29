package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.ItemGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ItemGridBlock extends GridBlock {
    private final MutableComponent name;

    public ItemGridBlock(MutableComponent name) {
        super(BlockConstants.STONE_PROPERTIES);
        this.name = name;
    }

    @Override
    public MutableComponent getName() {
        return this.name;
    }

    @Override
    protected BlockColorMap<?> getBlockColorMap() {
        return Blocks.INSTANCE.getGrid();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemGridBlockEntity(pos, state);
    }
}
