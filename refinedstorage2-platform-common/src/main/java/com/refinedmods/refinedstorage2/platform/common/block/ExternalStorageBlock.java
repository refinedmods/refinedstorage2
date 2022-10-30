package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.externalstorage.ExternalStorageBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.NetworkNodeBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ExternalStorageBlock extends AbstractDirectionalCableBlock implements EntityBlock {
    private static final AbstractBlockEntityTicker<ExternalStorageBlockEntity> TICKER =
        new NetworkNodeBlockEntityTicker<>(BlockEntities.INSTANCE::getExternalStorage);

    public ExternalStorageBlock() {
        super(BlockConstants.CABLE_PROPERTIES);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new ExternalStorageBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<T> type) {
        return TICKER.get(level, type);
    }

    @Override
    protected VoxelShape getExtensionShape(final Direction direction) {
        return switch (direction) {
            case NORTH -> DirectionalCableBlockShapes.EXTERNAL_STORAGE_NORTH;
            case EAST -> DirectionalCableBlockShapes.EXTERNAL_STORAGE_EAST;
            case SOUTH -> DirectionalCableBlockShapes.EXTERNAL_STORAGE_SOUTH;
            case WEST -> DirectionalCableBlockShapes.EXTERNAL_STORAGE_WEST;
            case UP -> DirectionalCableBlockShapes.EXTERNAL_STORAGE_UP;
            case DOWN -> DirectionalCableBlockShapes.EXTERNAL_STORAGE_DOWN;
        };
    }
}
