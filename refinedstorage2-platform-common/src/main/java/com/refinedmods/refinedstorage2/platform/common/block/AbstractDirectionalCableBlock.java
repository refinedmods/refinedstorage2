package com.refinedmods.refinedstorage2.platform.common.block;

import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractDirectionalCableBlock<T extends Enum<T> & StringRepresentable>
    extends AbstractDirectionalBlock<T>
    implements SimpleWaterloggedBlock {

    public AbstractDirectionalCableBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected BlockState getDefaultState() {
        return CableBlockSupport.getDefaultState(super.getDefaultState());
    }

    @Override
    public boolean propagatesSkylightDown(final BlockState state, final BlockGetter blockGetter, final BlockPos pos) {
        return !state.getValue(BlockStateProperties.WATERLOGGED);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(final BlockState state) {
        return Boolean.TRUE.equals(state.getValue(BlockStateProperties.WATERLOGGED))
            ? Fluids.WATER.getSource(false)
            : super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state,
                                  final Direction direction,
                                  final BlockState newState,
                                  final LevelAccessor level,
                                  final BlockPos pos,
                                  final BlockPos posFrom) {
        return CableBlockSupport.getState(state, level, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isPathfindable(final BlockState state,
                                  final BlockGetter world,
                                  final BlockPos pos,
                                  final PathComputationType type) {
        return false;
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext ctx) {
        return CableBlockSupport.getState(
            Objects.requireNonNull(super.getStateForPlacement(ctx)),
            ctx.getLevel(),
            ctx.getClickedPos()
        );
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        CableBlockSupport.appendBlockStateProperties(builder);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(final BlockState state,
                               final BlockGetter world,
                               final BlockPos pos,
                               final CollisionContext context) {
        return CableBlockSupport.getShape(state);
    }
}
