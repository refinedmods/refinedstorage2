package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.common.content.BlockConstants;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirectionType;
import com.refinedmods.refinedstorage2.platform.common.support.direction.DirectionType;

import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PortableGridBlock extends AbstractDirectionalBlock<BiDirection> implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final VoxelShape SHAPE_HORIZONTAL = box(0, 0, 0, 16, 13.2, 16);
    private static final VoxelShape SHAPE_VERTICAL_SOUTH = box(0, 0, 0, 16, 16, 13.2);
    private static final VoxelShape SHAPE_VERTICAL_NORTH = box(0, 0, 16 - 13.2, 16, 16, 16);
    private static final VoxelShape SHAPE_VERTICAL_EAST = box(0, 0, 0, 13.2, 16, 16);
    private static final VoxelShape SHAPE_VERTICAL_WEST = box(16 - 13.2, 0, 0, 16, 16, 16);

    private final PortableGridBlockEntityTicker ticker;
    private final BiFunction<BlockPos, BlockState, AbstractPortableGridBlockEntity> blockEntityFactory;

    public PortableGridBlock(final PortableGridType type,
                             final BiFunction<BlockPos, BlockState, AbstractPortableGridBlockEntity> factory) {
        super(BlockConstants.PROPERTIES);
        this.ticker = new PortableGridBlockEntityTicker(() -> type == PortableGridType.NORMAL
            ? BlockEntities.INSTANCE.getPortableGrid()
            : BlockEntities.INSTANCE.getCreativePortableGrid());
        this.blockEntityFactory = factory;
    }

    @Override
    protected BlockState getDefaultState() {
        return super.getDefaultState().setValue(ACTIVE, false);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    protected DirectionType<BiDirection> getDirectionType() {
        return BiDirectionType.INSTANCE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(final BlockState state,
                               final BlockGetter level,
                               final BlockPos pos,
                               final CollisionContext ctx) {
        final BiDirection direction = getDirection(state);
        if (direction == null) {
            return SHAPE_HORIZONTAL;
        }
        return switch (direction) {
            case UP_SOUTH, DOWN_SOUTH -> SHAPE_VERTICAL_SOUTH;
            case UP_NORTH, DOWN_NORTH -> SHAPE_VERTICAL_NORTH;
            case UP_EAST, DOWN_EAST -> SHAPE_VERTICAL_EAST;
            case UP_WEST, DOWN_WEST -> SHAPE_VERTICAL_WEST;
            default -> SHAPE_HORIZONTAL;
        };
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        return blockEntityFactory.apply(blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> type) {
        return ticker.get(level, type);
    }

    // TODO: can we make everything extended menu provider instead of wrapping it this way?
    @Override
    @SuppressWarnings("deprecation") // Forge deprecates this
    public MenuProvider getMenuProvider(final BlockState state, final Level level, final BlockPos pos) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof Grid grid && blockEntity instanceof MenuProvider menuProvider) {
            return PlatformApi.INSTANCE.getGridMenuProvider(grid, menuProvider);
        }
        return null;
    }
}
