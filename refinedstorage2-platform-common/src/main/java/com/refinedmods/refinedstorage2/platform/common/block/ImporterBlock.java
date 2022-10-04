package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.ImporterBlockEntity;
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

public class ImporterBlock extends AbstractDirectionalCableBlock implements EntityBlock {
    private static final AbstractBlockEntityTicker<ImporterBlockEntity> TICKER =
        new NetworkNodeBlockEntityTicker<>(BlockEntities.INSTANCE::getImporter);

    public ImporterBlock() {
        super(BlockConstants.CABLE_PROPERTIES);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new ImporterBlockEntity(pos, state);
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
            case NORTH -> DirectionalCableBlockShapes.IMPORTER_NORTH;
            case EAST -> DirectionalCableBlockShapes.IMPORTER_EAST;
            case SOUTH -> DirectionalCableBlockShapes.IMPORTER_SOUTH;
            case WEST -> DirectionalCableBlockShapes.IMPORTER_WEST;
            case UP -> DirectionalCableBlockShapes.IMPORTER_UP;
            case DOWN -> DirectionalCableBlockShapes.IMPORTER_DOWN;
        };
    }
}
