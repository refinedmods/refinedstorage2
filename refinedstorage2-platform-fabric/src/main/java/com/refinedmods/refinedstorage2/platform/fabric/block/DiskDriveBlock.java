package com.refinedmods.refinedstorage2.platform.fabric.block;

import com.refinedmods.refinedstorage2.platform.fabric.block.entity.diskdrive.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.ticker.DiskDriveBlockEntityTicker;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DiskDriveBlock extends NetworkNodeContainerBlock {
    public DiskDriveBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean hasBiDirection() {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DiskDriveBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (BlockEntityTicker<T>) new DiskDriveBlockEntityTicker();
    }
}
