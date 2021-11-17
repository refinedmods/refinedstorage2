package com.refinedmods.refinedstorage2.platform.fabric.block.entity.ticker;

import com.refinedmods.refinedstorage2.platform.fabric.block.entity.diskdrive.DiskDriveBlockEntity;

import net.minecraft.world.level.block.state.BlockState;

public class DiskDriveBlockEntityTicker extends FabricNetworkNodeContainerBlockEntityTicker<DiskDriveBlockEntity> {
    @Override
    protected void performContainerUpdate(DiskDriveBlockEntity blockEntity, BlockState state) {
        super.performContainerUpdate(blockEntity, state);
        blockEntity.updateDiskStateIfNecessary();
    }
}
