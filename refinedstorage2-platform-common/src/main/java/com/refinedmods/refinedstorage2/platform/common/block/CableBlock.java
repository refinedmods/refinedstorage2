package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.CableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CableBlock extends NetworkNodeContainerBlock implements SimpleWaterloggedBlock {
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

    public CableBlock() {
        super(BlockBehaviour.Properties.of(Material.GLASS).sound(SoundType.GLASS).strength(0.35F, 0.35F));

        registerDefaultState(getStateDefinition().any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    public boolean propagatesSkylightDown(final BlockState state, final BlockGetter blockGetter, final BlockPos pos) {
        return !state.getValue(BlockStateProperties.WATERLOGGED);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(final BlockState state) {
        return Boolean.TRUE.equals(state.getValue(BlockStateProperties.WATERLOGGED)) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction direction, final BlockState newState, final LevelAccessor level, final BlockPos pos, final BlockPos posFrom) {
        return getState(state, level, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isPathfindable(final BlockState state, final BlockGetter world, final BlockPos pos, final PathComputationType type) {
        return false;
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext ctx) {
        return getState(defaultBlockState(), ctx.getLevel(), ctx.getClickedPos());
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, BlockStateProperties.WATERLOGGED);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(final BlockState state, final BlockGetter world, final BlockPos pos, final CollisionContext context) {
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

    private boolean hasConnection(final LevelAccessor world, final BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof NetworkNodeContainerBlock;
    }

    private BlockState getState(final BlockState currentState, final LevelAccessor world, final BlockPos pos) {
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

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new CableBlockEntity(pos, state);
    }
}
