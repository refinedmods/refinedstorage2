package com.refinedmods.refinedstorage2.platform.fabric.block.entity.ticker;

import com.refinedmods.refinedstorage2.platform.fabric.block.entity.diskdrive.DiskDriveBlockEntity;

import net.minecraft.block.BlockState;
import net.minecraft.world.World;

public class DiskDriveBlockEntityTicker extends NetworkNodeBlockEntityTicker<DiskDriveBlockEntity> {
    @Override
    protected void tick(World world, BlockState state, DiskDriveBlockEntity blockEntity) {
        super.tick(world, state, blockEntity);
        blockEntity.updateDiskStateIfNecessary();
    }
}
