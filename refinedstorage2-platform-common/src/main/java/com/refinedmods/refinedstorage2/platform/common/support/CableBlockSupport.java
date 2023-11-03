package com.refinedmods.refinedstorage2.platform.common.support;

import com.refinedmods.refinedstorage2.platform.api.support.network.PlatformNetworkNodeContainer;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

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

public final class CableBlockSupport {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    private static final VoxelShape SHAPE_CORE = box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape SHAPE_NORTH = box(6, 6, 0, 10, 10, 6);
    private static final VoxelShape SHAPE_EAST = box(10, 6, 6, 16, 10, 10);
    private static final VoxelShape SHAPE_SOUTH = box(6, 6, 10, 10, 10, 16);
    private static final VoxelShape SHAPE_WEST = box(0, 6, 6, 6, 10, 10);
    private static final VoxelShape SHAPE_UP = box(6, 10, 6, 10, 16, 10);
    private static final VoxelShape SHAPE_DOWN = box(6, 0, 6, 10, 6, 10);
    private static final Map<CableShapeCacheKey, VoxelShape> SHAPE_CACHE = new HashMap<>();

    private CableBlockSupport() {
    }

    public static BlockState getDefaultState(final BlockState state) {
        return state
            .setValue(NORTH, false)
            .setValue(EAST, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(UP, false)
            .setValue(DOWN, false)
            .setValue(BlockStateProperties.WATERLOGGED, false);
    }

    public static void appendBlockStateProperties(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, BlockStateProperties.WATERLOGGED);
    }

    public static VoxelShape getShape(final CableShapeCacheKey cacheKey) {
        return SHAPE_CACHE.computeIfAbsent(cacheKey, CableBlockSupport::calculateShape);
    }

    private static VoxelShape calculateShape(final CableShapeCacheKey cacheKey) {
        VoxelShape shape = SHAPE_CORE;
        if (cacheKey.north()) {
            shape = Shapes.or(shape, SHAPE_NORTH);
        }
        if (cacheKey.east()) {
            shape = Shapes.or(shape, SHAPE_EAST);
        }
        if (cacheKey.south()) {
            shape = Shapes.or(shape, SHAPE_SOUTH);
        }
        if (cacheKey.west()) {
            shape = Shapes.or(shape, SHAPE_WEST);
        }
        if (cacheKey.up()) {
            shape = Shapes.or(shape, SHAPE_UP);
        }
        if (cacheKey.down()) {
            shape = Shapes.or(shape, SHAPE_DOWN);
        }
        return shape;
    }

    public static BlockState getState(
        final BlockState currentState,
        final LevelAccessor world,
        final BlockPos pos,
        @Nullable final Direction blacklistedDirection
    ) {
        final boolean north = hasVisualConnection(currentState, world, pos, Direction.NORTH, blacklistedDirection);
        final boolean east = hasVisualConnection(currentState, world, pos, Direction.EAST, blacklistedDirection);
        final boolean south = hasVisualConnection(currentState, world, pos, Direction.SOUTH, blacklistedDirection);
        final boolean west = hasVisualConnection(currentState, world, pos, Direction.WEST, blacklistedDirection);
        final boolean up = hasVisualConnection(currentState, world, pos, Direction.UP, blacklistedDirection);
        final boolean down = hasVisualConnection(currentState, world, pos, Direction.DOWN, blacklistedDirection);

        return currentState
            .setValue(NORTH, north)
            .setValue(EAST, east)
            .setValue(SOUTH, south)
            .setValue(WEST, west)
            .setValue(UP, up)
            .setValue(DOWN, down);
    }

    private static boolean hasVisualConnection(
        final BlockState blockState,
        final LevelAccessor world,
        final BlockPos pos,
        final Direction direction,
        @Nullable final Direction blacklistedDirection
    ) {
        if (direction == blacklistedDirection) {
            return false;
        }
        final BlockPos offsetPos = pos.relative(direction);
        if (!(world.getBlockEntity(offsetPos) instanceof PlatformNetworkNodeContainer neighboringContainer)) {
            return false;
        }
        return neighboringContainer.canAcceptIncomingConnection(direction.getOpposite(), blockState);
    }
}
