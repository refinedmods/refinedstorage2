package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.AbstractGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.AbstractBlockEntityTicker;

import javax.annotation.Nullable;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class AbstractGridBlock<T extends AbstractGridBlockEntity<?>> extends AbstractBaseBlock
    implements EntityBlock {
    protected static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final AbstractBlockEntityTicker<T> ticker;

    protected AbstractGridBlock(final Properties properties, final AbstractBlockEntityTicker<T> ticker) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(ACTIVE, false));
        this.ticker = ticker;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    protected boolean hasBiDirection() {
        return true;
    }

    @Nullable
    @Override
    public <O extends BlockEntity> BlockEntityTicker<O> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<O> type) {
        return ticker.get(level, type);
    }
}
