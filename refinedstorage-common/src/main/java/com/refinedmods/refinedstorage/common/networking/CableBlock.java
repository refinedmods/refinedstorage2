package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.BlockEntityProvider;
import com.refinedmods.refinedstorage.common.content.BlockProperties;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage.common.support.AbstractColoredBlock;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.CableShapes;
import com.refinedmods.refinedstorage.common.support.ColorableBlock;
import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.common.support.network.NetworkNodeBlockEntityTicker;
import com.refinedmods.refinedstorage.common.util.PlatformUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class CableBlock extends AbstractColoredBlock<CableBlock>
    implements ColorableBlock<CableBlock, BaseBlockItem>, SimpleWaterloggedBlock, EntityBlock {
    private static final AbstractBlockEntityTicker<AbstractCableBlockEntity> TICKER =
        new NetworkNodeBlockEntityTicker<>(
            BlockEntities.INSTANCE::getCable
        );
    private static final Component HELP = createTranslation("item", "cable.help");

    private final Identifier id;
    private final BlockEntityProvider<AbstractCableBlockEntity> blockEntityProvider;

    public CableBlock(final Identifier id,
                      final DyeColor color,
                      final MutableComponent name,
                      final BlockEntityProvider<AbstractCableBlockEntity> blockEntityProvider) {
        super(BlockProperties.cable(id), color, name);
        this.id = id;
        this.blockEntityProvider = blockEntityProvider;
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
        if (blockEntity instanceof AbstractCableBlockEntity cable) {
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
        if (!(blockEntity instanceof AbstractCableBlockEntity cable)) {
            return Shapes.block();
        }
        final CableConnections connections = cable.getConnections();
        return CableShapes.getShape(connections);
    }

    @Override
    protected void onPlace(final BlockState state,
                           final Level level,
                           final BlockPos pos,
                           final BlockState oldState,
                           final boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AbstractCableBlockEntity cable) {
            cable.updateConnections();
            PlatformUtil.sendBlockUpdateToClient(level, pos);
        }
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return blockEntityProvider.create(pos, state);
    }

    @Override
    public BlockColorMap<CableBlock, BaseBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getCable();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<T> type) {
        return TICKER.get(level, type);
    }

    @Override
    public BaseBlockItem createBlockItem() {
        return new NetworkNodeBlockItem(id, this, HELP);
    }
}
