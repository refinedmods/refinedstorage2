package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.Block.box;

final class CableBlockSupport {
    private static final BooleanProperty NORTH = BooleanProperty.create("north");
    private static final BooleanProperty EAST = BooleanProperty.create("east");
    private static final BooleanProperty SOUTH = BooleanProperty.create("south");
    private static final BooleanProperty WEST = BooleanProperty.create("west");
    private static final BooleanProperty UP = BooleanProperty.create("up");
    private static final BooleanProperty DOWN = BooleanProperty.create("down");

    private static final VoxelShape SHAPE_CORE = box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape SHAPE_NORTH = box(6, 6, 0, 10, 10, 6);
    private static final VoxelShape SHAPE_EAST = box(10, 6, 6, 16, 10, 10);
    private static final VoxelShape SHAPE_SOUTH = box(6, 6, 10, 10, 10, 16);
    private static final VoxelShape SHAPE_WEST = box(0, 6, 6, 6, 10, 10);
    private static final VoxelShape SHAPE_UP = box(6, 10, 6, 10, 16, 10);
    private static final VoxelShape SHAPE_DOWN = box(6, 0, 6, 10, 6, 10);

    private CableBlockSupport() {
    }

    static BlockState getDefaultState(final BlockState state) {
        return state
            .setValue(NORTH, false)
            .setValue(EAST, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(UP, false)
            .setValue(DOWN, false)
            .setValue(BlockStateProperties.WATERLOGGED, false);
    }

    static void appendBlockStateProperties(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, BlockStateProperties.WATERLOGGED);
    }

    static VoxelShape getShape(final BlockState state) {
        VoxelShape shape = SHAPE_CORE;
        if (Boolean.TRUE.equals(state.getValue(NORTH))) {
            shape = Shapes.or(shape, SHAPE_NORTH);
        }
        if (Boolean.TRUE.equals(state.getValue(EAST))) {
            shape = Shapes.or(shape, SHAPE_EAST);
        }
        if (Boolean.TRUE.equals(state.getValue(SOUTH))) {
            shape = Shapes.or(shape, SHAPE_SOUTH);
        }
        if (Boolean.TRUE.equals(state.getValue(WEST))) {
            shape = Shapes.or(shape, SHAPE_WEST);
        }
        if (Boolean.TRUE.equals(state.getValue(UP))) {
            shape = Shapes.or(shape, SHAPE_UP);
        }
        if (Boolean.TRUE.equals(state.getValue(DOWN))) {
            shape = Shapes.or(shape, SHAPE_DOWN);
        }
        return shape;
    }

    static BlockState getState(final BlockState currentState, final LevelAccessor world, final BlockPos pos) {
        final boolean north = hasConnection(world, pos.relative(Direction.NORTH));
        final boolean east = hasConnection(world, pos.relative(Direction.EAST));
        final boolean south = hasConnection(world, pos.relative(Direction.SOUTH));
        final boolean west = hasConnection(world, pos.relative(Direction.WEST));
        final boolean up = hasConnection(world, pos.relative(Direction.UP));
        final boolean down = hasConnection(world, pos.relative(Direction.DOWN));

        return currentState
            .setValue(NORTH, north)
            .setValue(EAST, east)
            .setValue(SOUTH, south)
            .setValue(WEST, west)
            .setValue(UP, up)
            .setValue(DOWN, down);
    }

    private static boolean hasConnection(final LevelAccessor world, final BlockPos pos) {
        return world.getBlockEntity(pos) instanceof NetworkNodeContainer;
    }
}
