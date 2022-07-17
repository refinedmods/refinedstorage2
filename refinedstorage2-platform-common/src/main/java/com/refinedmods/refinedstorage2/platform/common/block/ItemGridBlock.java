package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.ItemGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.NetworkNodeBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ItemGridBlock extends AbstractGridBlock<ItemGridBlockEntity> implements ColorableBlock<ItemGridBlock> {
    private final MutableComponent name;

    public ItemGridBlock(final MutableComponent name) {
        super(
            BlockConstants.PROPERTIES,
            new NetworkNodeBlockEntityTicker<>(BlockEntities.INSTANCE::getGrid, ACTIVE)
        );
        this.name = name;
    }

    @Override
    public MutableComponent getName() {
        return this.name;
    }

    @Override
    public BlockColorMap<ItemGridBlock> getBlockColorMap() {
        return Blocks.INSTANCE.getGrid();
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new ItemGridBlockEntity(pos, state);
    }
}
