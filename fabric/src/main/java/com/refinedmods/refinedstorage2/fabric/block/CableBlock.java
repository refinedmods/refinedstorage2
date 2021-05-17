package com.refinedmods.refinedstorage2.fabric.block;

import com.refinedmods.refinedstorage2.fabric.block.entity.CableBlockEntity;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class CableBlock extends NetworkNodeBlock implements Waterloggable {
    private static final BooleanProperty NORTH = BooleanProperty.of("north");
    private static final BooleanProperty EAST = BooleanProperty.of("east");
    private static final BooleanProperty SOUTH = BooleanProperty.of("south");
    private static final BooleanProperty WEST = BooleanProperty.of("west");
    private static final BooleanProperty UP = BooleanProperty.of("up");
    private static final BooleanProperty DOWN = BooleanProperty.of("down");

    private static final VoxelShape SHAPE_CORE = createCuboidShape(6, 6, 6, 10, 10, 10);
    private static final VoxelShape SHAPE_NORTH = createCuboidShape(6, 6, 0, 10, 10, 6);
    private static final VoxelShape SHAPE_EAST = createCuboidShape(10, 6, 6, 16, 10, 10);
    private static final VoxelShape SHAPE_SOUTH = createCuboidShape(6, 6, 10, 10, 10, 16);
    private static final VoxelShape SHAPE_WEST = createCuboidShape(0, 6, 6, 6, 10, 10);
    private static final VoxelShape SHAPE_UP = createCuboidShape(6, 10, 6, 10, 16, 10);
    private static final VoxelShape SHAPE_DOWN = createCuboidShape(6, 0, 6, 10, 6, 10);

    public CableBlock() {
        super(FabricBlockSettings.of(Material.GLASS).sounds(BlockSoundGroup.GLASS).strength(0.35F, 0.35F));

        setDefaultState(getStateManager().getDefaultState()
                .with(NORTH, false)
                .with(EAST, false)
                .with(SOUTH, false)
                .with(WEST, false)
                .with(UP, false)
                .with(DOWN, false)
                .with(Properties.WATERLOGGED, false));
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return !state.get(Properties.WATERLOGGED);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return Boolean.TRUE.equals(state.get(Properties.WATERLOGGED)) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        return getState(state, world, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return getState(getDefaultState(), ctx.getWorld(), ctx.getBlockPos());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);

        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, Properties.WATERLOGGED);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = SHAPE_CORE;

        if (Boolean.TRUE.equals(state.get(NORTH))) {
            shape = VoxelShapes.union(shape, SHAPE_NORTH);
        }

        if (Boolean.TRUE.equals(state.get(EAST))) {
            shape = VoxelShapes.union(shape, SHAPE_EAST);
        }

        if (Boolean.TRUE.equals(state.get(SOUTH))) {
            shape = VoxelShapes.union(shape, SHAPE_SOUTH);
        }

        if (Boolean.TRUE.equals(state.get(WEST))) {
            shape = VoxelShapes.union(shape, SHAPE_WEST);
        }

        if (Boolean.TRUE.equals(state.get(UP))) {
            shape = VoxelShapes.union(shape, SHAPE_UP);
        }

        if (Boolean.TRUE.equals(state.get(DOWN))) {
            shape = VoxelShapes.union(shape, SHAPE_DOWN);
        }

        return shape;
    }

    private boolean hasConnection(WorldAccess world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof NetworkNodeBlock;
    }

    private BlockState getState(BlockState currentState, WorldAccess world, BlockPos pos) {
        boolean north = hasConnection(world, pos.offset(Direction.NORTH));
        boolean east = hasConnection(world, pos.offset(Direction.EAST));
        boolean south = hasConnection(world, pos.offset(Direction.SOUTH));
        boolean west = hasConnection(world, pos.offset(Direction.WEST));
        boolean up = hasConnection(world, pos.offset(Direction.UP));
        boolean down = hasConnection(world, pos.offset(Direction.DOWN));

        return currentState
                .with(NORTH, north)
                .with(EAST, east)
                .with(SOUTH, south)
                .with(WEST, west)
                .with(UP, up)
                .with(DOWN, down);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockView world) {
        return new CableBlockEntity();
    }
}
