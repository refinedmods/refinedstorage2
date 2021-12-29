package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DiskDriveBlock extends NetworkNodeContainerBlock {
    public DiskDriveBlock() {
        super(BlockConstants.STONE_PROPERTIES);
    }

    @Override
    protected boolean hasBiDirection() {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DiskDriveBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntities.INSTANCE.getDiskDrive() && !level.isClientSide ? (level2, pos, state2, blockEntity) -> DiskDriveBlockEntity.serverTick(level2, state2, (DiskDriveBlockEntity) blockEntity) : null;
    }
}
