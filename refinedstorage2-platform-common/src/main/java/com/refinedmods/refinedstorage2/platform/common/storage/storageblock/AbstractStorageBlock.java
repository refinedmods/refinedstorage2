package com.refinedmods.refinedstorage2.platform.common.storage.storageblock;

import com.refinedmods.refinedstorage2.platform.common.support.AbstractBaseBlock;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBlockEntityTicker;

import javax.annotation.Nullable;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractStorageBlock<T extends AbstractStorageBlockBlockEntity<?>> extends AbstractBaseBlock
    implements EntityBlock {
    private final AbstractBlockEntityTicker<T> ticker;

    protected AbstractStorageBlock(final Properties properties, final AbstractBlockEntityTicker<T> ticker) {
        super(properties);
        this.ticker = ticker;
    }

    @Nullable
    @Override
    public <O extends BlockEntity> BlockEntityTicker<O> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<O> type) {
        return ticker.get(level, type);
    }
}
