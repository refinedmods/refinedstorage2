package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.content.BlockProperties;
import com.refinedmods.refinedstorage.common.networking.CableConnections;
import com.refinedmods.refinedstorage.common.support.direction.DefaultDirectionType;
import com.refinedmods.refinedstorage.common.support.direction.DirectionType;
import com.refinedmods.refinedstorage.common.util.PlatformUtil;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class AbstractDirectionalCableBlock extends AbstractDirectionalBlock<Direction>
    implements SimpleWaterloggedBlock {
    private final ConcurrentHashMap<DirectionalCacheShapeCacheKey, VoxelShape> shapeCache;

    protected AbstractDirectionalCableBlock(final Identifier id,
                                            final ConcurrentHashMap<DirectionalCacheShapeCacheKey, VoxelShape>
                                                shapeCache) {
        super(BlockProperties.cable(id));
        this.shapeCache = shapeCache;
    }

    @Override
    protected DirectionType<Direction> getDirectionType() {
        return DefaultDirectionType.FACE_CLICKED;
    }

    @Override
    protected BlockState getDefaultState() {
        return super.getDefaultState().setValue(BlockStateProperties.WATERLOGGED, false);
    }

    @Override
    protected boolean propagatesSkylightDown(final BlockState state) {
        return !state.getValue(BlockStateProperties.WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(final BlockState state) {
        return Boolean.TRUE.equals(state.getValue(BlockStateProperties.WATERLOGGED))
            ? Fluids.WATER.getSource(false)
            : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    protected boolean isPathfindable(final BlockState state, final PathComputationType type) {
        return false;
    }

    @Override
    protected BlockState updateShape(final BlockState state, final LevelReader level, final ScheduledTickAccess ticks,
                                     final BlockPos pos,
                                     final Direction directionToNeighbour, final BlockPos neighbourPos,
                                     final BlockState neighbourState,
                                     final RandomSource random) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AbstractCableLikeBlockEntity<?> cable) {
            cable.updateConnections();
            if (level instanceof ServerLevel serverLevel) {
                PlatformUtil.sendBlockUpdateToClient(serverLevel, pos);
            }
            if (level.isClientSide()) {
                Platform.INSTANCE.requestModelDataUpdateOnClient(blockEntity, false);
            }
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    public VoxelShape getShape(final BlockState state,
                               final BlockGetter world,
                               final BlockPos pos,
                               final CollisionContext context) {
        final BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof AbstractCableLikeBlockEntity<?> cable)) {
            return Shapes.block();
        }
        final CableConnections connections = cable.getConnections();
        final Direction direction = getDirection(state);
        if (direction == null) {
            return CableShapes.getShape(connections);
        }
        final DirectionalCacheShapeCacheKey directionalCacheKey = new DirectionalCacheShapeCacheKey(
            connections,
            direction
        );
        return shapeCache.computeIfAbsent(directionalCacheKey, this::computeShape);
    }

    private VoxelShape computeShape(final DirectionalCacheShapeCacheKey cacheKey) {
        return Shapes.or(
            CableShapes.getShape(cacheKey.connections),
            getExtensionShape(cacheKey.direction)
        );
    }

    @Override
    protected void onPlace(final BlockState state,
                           final Level level,
                           final BlockPos pos,
                           final BlockState oldState,
                           final boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AbstractCableLikeBlockEntity<?> cable) {
            cable.updateConnections();
            PlatformUtil.sendBlockUpdateToClient(level, pos);
        }
    }

    @Override
    @Nullable
    protected VoxelShape getScreenOpenableShape(final BlockState state) {
        final Direction direction = getDirection(state);
        if (direction == null) {
            return Shapes.empty();
        }
        return getExtensionShape(direction);
    }

    protected abstract VoxelShape getExtensionShape(Direction direction);

    protected record DirectionalCacheShapeCacheKey(CableConnections connections, Direction direction) {
    }
}
