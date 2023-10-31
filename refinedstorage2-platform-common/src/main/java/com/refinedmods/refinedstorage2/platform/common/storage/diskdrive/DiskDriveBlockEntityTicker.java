package com.refinedmods.refinedstorage2.platform.common.storage.diskdrive;

import com.refinedmods.refinedstorage2.api.network.impl.node.multistorage.MultiStorageNetworkNode;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.support.NetworkNodeBlockEntityTicker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DiskDriveBlockEntityTicker
    extends NetworkNodeBlockEntityTicker<MultiStorageNetworkNode, AbstractDiskDriveBlockEntity> {

    @SuppressWarnings("unchecked")
    public DiskDriveBlockEntityTicker() {
        super(() -> (BlockEntityType<AbstractDiskDriveBlockEntity>) BlockEntities.INSTANCE.getDiskDrive());
    }

    @Override
    public void tick(final Level level,
                     final BlockPos pos,
                     final BlockState state,
                     final AbstractDiskDriveBlockEntity blockEntity) {
        super.tick(level, pos, state, blockEntity);
        blockEntity.updateDiskStateIfNecessaryInLevel();
    }
}
