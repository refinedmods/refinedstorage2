package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;

import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DiskDriveBlock extends AbstractNetworkNodeContainerBlock {
    private final BiFunction<BlockPos, BlockState, AbstractDiskDriveBlockEntity> blockEntityFactory;

    public DiskDriveBlock(final BiFunction<BlockPos, BlockState, AbstractDiskDriveBlockEntity> blockEntityFactory) {
        super(BlockConstants.STONE_PROPERTIES);
        this.blockEntityFactory = blockEntityFactory;
    }

    @Override
    protected boolean hasBiDirection() {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return blockEntityFactory.apply(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> type) {
        return type == BlockEntities.INSTANCE.getDiskDrive() && !level.isClientSide
                ? (l, p, s, be) -> AbstractDiskDriveBlockEntity.serverTick(s, (AbstractDiskDriveBlockEntity) be)
                : null;
    }
}
