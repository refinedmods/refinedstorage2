package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.ImporterBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.NetworkNodeBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ImporterBlock extends AbstractDirectionalCableBlock implements EntityBlock {
    private static final VoxelShape LINE_NORTH_1 = box(6, 6, 4, 10, 10, 6);
    private static final VoxelShape LINE_NORTH_2 = box(5, 5, 2, 11, 11, 4);
    private static final VoxelShape LINE_NORTH_3 = box(3, 3, 0, 13, 13, 2);
    private static final VoxelShape LINE_NORTH = Shapes.or(LINE_NORTH_1, LINE_NORTH_2, LINE_NORTH_3);

    private static final VoxelShape LINE_EAST_1 = box(10, 6, 6, 12, 10, 10);
    private static final VoxelShape LINE_EAST_2 = box(12, 5, 5, 14, 11, 11);
    private static final VoxelShape LINE_EAST_3 = box(14, 3, 3, 16, 13, 13);
    private static final VoxelShape LINE_EAST = Shapes.or(LINE_EAST_1, LINE_EAST_2, LINE_EAST_3);

    private static final VoxelShape LINE_SOUTH_1 = box(6, 6, 10, 10, 10, 12);
    private static final VoxelShape LINE_SOUTH_2 = box(5, 5, 12, 11, 11, 14);
    private static final VoxelShape LINE_SOUTH_3 = box(3, 3, 14, 13, 13, 16);
    private static final VoxelShape LINE_SOUTH = Shapes.or(LINE_SOUTH_1, LINE_SOUTH_2, LINE_SOUTH_3);

    private static final VoxelShape LINE_WEST_1 = box(4, 6, 6, 6, 10, 10);
    private static final VoxelShape LINE_WEST_2 = box(2, 5, 5, 4, 11, 11);
    private static final VoxelShape LINE_WEST_3 = box(0, 3, 3, 2, 13, 13);
    private static final VoxelShape LINE_WEST = Shapes.or(LINE_WEST_1, LINE_WEST_2, LINE_WEST_3);

    private static final VoxelShape LINE_UP_1 = box(6, 10, 6, 10, 12, 10);
    private static final VoxelShape LINE_UP_2 = box(5, 12, 5, 11, 14, 11);
    private static final VoxelShape LINE_UP_3 = box(3, 14, 3, 13, 16, 13);
    private static final VoxelShape LINE_UP = Shapes.or(LINE_UP_1, LINE_UP_2, LINE_UP_3);

    private static final VoxelShape LINE_DOWN_1 = box(6, 4, 6, 10, 6, 10);
    private static final VoxelShape LINE_DOWN_2 = box(5, 2, 5, 11, 4, 11);
    private static final VoxelShape LINE_DOWN_3 = box(3, 0, 3, 13, 2, 13);
    private static final VoxelShape LINE_DOWN = Shapes.or(LINE_DOWN_1, LINE_DOWN_2, LINE_DOWN_3);

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
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(final BlockState state,
                               final BlockGetter world,
                               final BlockPos pos,
                               final CollisionContext ctx) {
        return Shapes.or(super.getShape(state, world, pos, ctx), getLineShape(state));
    }

    private VoxelShape getLineShape(final BlockState state) {
        final Direction direction = getDirection(state);
        if (direction == null) {
            return Shapes.empty();
        }
        return switch (direction) {
            case NORTH -> LINE_NORTH;
            case EAST -> LINE_EAST;
            case SOUTH -> LINE_SOUTH;
            case WEST -> LINE_WEST;
            case UP -> LINE_UP;
            case DOWN -> LINE_DOWN;
        };
    }
}