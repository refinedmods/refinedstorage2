package com.refinedmods.refinedstorage2.platform.fabric.block;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.diskdrive.DiskDriveBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DiskDriveBlock extends NetworkNodeContainerBlock {
    public DiskDriveBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean hasBiDirection() {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DiskDriveBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == Rs2Mod.BLOCK_ENTITIES.getDiskDrive() && !level.isClientSide ? (level2, pos, state2, blockEntity) -> DiskDriveBlockEntity.serverTick(level2, pos, state2, (DiskDriveBlockEntity) blockEntity) : null;
    }
}
