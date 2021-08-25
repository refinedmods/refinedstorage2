package com.refinedmods.refinedstorage2.platform.fabric.api.block.entity.ticker;

import com.refinedmods.refinedstorage2.platform.fabric.api.block.entity.NetworkNodeContainerBlockEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NetworkNodeContainerBlockEntityTicker<T extends BlockEntity & NetworkNodeContainerBlockEntity<?>> implements BlockEntityTicker<T> {
    @Override
    public void tick(World world, BlockPos pos, BlockState state, T blockEntity) {
        if (world.isClient()) {
            return;
        }
        performContainerUpdate(blockEntity, state);
    }

    protected void performContainerUpdate(T blockEntity, BlockState state) {
        blockEntity.getContainer().initialize();
        blockEntity.getContainer().update();
    }
}
