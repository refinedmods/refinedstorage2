package com.refinedmods.refinedstorage2.platform.fabric.block;

import com.refinedmods.refinedstorage2.platform.fabric.block.entity.RelayBlockEntity;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RelayBlock extends NetworkNodeContainerBlock {
    public static final DirectionProperty DIRECTION = DirectionProperty.create("direction", Direction.values());
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

    public RelayBlock() {
        super(FabricBlockSettings.of(Material.GLASS).sound(SoundType.GLASS).strength(0.35F, 0.35F));

        registerDefaultState(getStateDefinition().any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(DIRECTION, Direction.NORTH));
    }

    @Override
    protected boolean hasActive() {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState newState, LevelAccessor world, BlockPos pos, BlockPos posFrom) {
        return getState(state, world, pos, state.getValue(DIRECTION));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return getState(defaultBlockState(), ctx.getLevel(), ctx.getClickedPos(), ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, DIRECTION);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
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

        if (state.getValue(DIRECTION) == Direction.NORTH) {
            shape = Shapes.or(shape, SHAPE_NORTH);
        }

        if (state.getValue(DIRECTION) == Direction.EAST) {
            shape = Shapes.or(shape, SHAPE_EAST);
        }

        if (state.getValue(DIRECTION) == Direction.SOUTH) {
            shape = Shapes.or(shape, SHAPE_SOUTH);
        }

        if (state.getValue(DIRECTION) == Direction.WEST) {
            shape = Shapes.or(shape, SHAPE_WEST);
        }

        if (state.getValue(DIRECTION) == Direction.UP) {
            shape = Shapes.or(shape, SHAPE_UP);
        }

        if (state.getValue(DIRECTION) == Direction.DOWN) {
            shape = Shapes.or(shape, SHAPE_DOWN);
        }

        return shape;
    }

    private boolean hasConnection(LevelAccessor world, BlockPos pos, Direction connectionDirection, Direction direction) {
        if (direction == connectionDirection) {
            return false;
        }
        return world.getBlockState(pos.relative(connectionDirection)).getBlock() instanceof NetworkNodeContainerBlock;
    }

    private BlockState getState(BlockState currentState, LevelAccessor world, BlockPos pos, Direction direction) {
        boolean north = hasConnection(world, pos, Direction.NORTH, direction);
        boolean east = hasConnection(world, pos, Direction.EAST, direction);
        boolean south = hasConnection(world, pos, Direction.SOUTH, direction);
        boolean west = hasConnection(world, pos, Direction.WEST, direction);
        boolean up = hasConnection(world, pos, Direction.UP, direction);
        boolean down = hasConnection(world, pos, Direction.DOWN, direction);

        return currentState
                .setValue(NORTH, north)
                .setValue(EAST, east)
                .setValue(SOUTH, south)
                .setValue(WEST, west)
                .setValue(UP, up)
                .setValue(DOWN, down)
                .setValue(DIRECTION, direction);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RelayBlockEntity(pos, state);
    }
}
