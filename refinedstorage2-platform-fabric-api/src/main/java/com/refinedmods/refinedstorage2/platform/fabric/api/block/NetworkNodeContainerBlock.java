package com.refinedmods.refinedstorage2.platform.fabric.api.block;

import com.refinedmods.refinedstorage2.platform.fabric.api.block.entity.ticker.NetworkNodeContainerBlockEntityTicker;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class NetworkNodeContainerBlock extends Block implements EntityBlock {
    public NetworkNodeContainerBlock(Properties settings) {
        super(settings);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return (BlockEntityTicker<T>) new NetworkNodeContainerBlockEntityTicker<>();
    }
}
