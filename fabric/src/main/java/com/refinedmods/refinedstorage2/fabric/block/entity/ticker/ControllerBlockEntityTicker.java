package com.refinedmods.refinedstorage2.fabric.block.entity.ticker;

import com.refinedmods.refinedstorage2.fabric.block.entity.ControllerBlockEntity;

import net.minecraft.block.BlockState;
import net.minecraft.world.World;

public class ControllerBlockEntityTicker extends NetworkNodeBlockEntityTicker<ControllerBlockEntity> {
    @Override
    protected void tick(World world, BlockState state, ControllerBlockEntity blockEntity) {
        super.tick(world, state, blockEntity);
        blockEntity.updateEnergyType(state);
    }
}
