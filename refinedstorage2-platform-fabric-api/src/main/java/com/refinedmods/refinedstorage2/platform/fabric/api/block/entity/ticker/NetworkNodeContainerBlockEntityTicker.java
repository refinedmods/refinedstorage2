package com.refinedmods.refinedstorage2.platform.fabric.api.block.entity.ticker;

import com.refinedmods.refinedstorage2.platform.fabric.api.block.entity.NetworkNodeContainerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

public class NetworkNodeContainerBlockEntityTicker<T extends BlockEntity & NetworkNodeContainerBlockEntity<?>> implements BlockEntityTicker<T> {
    @Override
    public void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (level.isClientSide()) {
            return;
        }
        performContainerUpdate(blockEntity, state);
    }

    protected void performContainerUpdate(T blockEntity, BlockState state) {
        blockEntity.getContainer().initialize();
        blockEntity.getContainer().update();
    }
}
