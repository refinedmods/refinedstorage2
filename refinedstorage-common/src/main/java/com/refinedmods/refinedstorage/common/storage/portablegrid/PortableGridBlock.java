package com.refinedmods.refinedstorage.common.storage.portablegrid;

import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.BlockEntityProvider;
import com.refinedmods.refinedstorage.common.content.BlockProperties;
import com.refinedmods.refinedstorage.common.content.ContentIds;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.direction.DirectionType;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirectionType;

import net.minecraft.core.BlockPos;
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
import org.jspecify.annotations.Nullable;

public class PortableGridBlock extends AbstractDirectionalBlock<OrientedDirection> implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final VoxelShape SHAPE_HORIZONTAL = box(0, 0, 0, 16, 13.2, 16);
    private static final VoxelShape SHAPE_VERTICAL_SOUTH = box(0, 0, 0, 16, 16, 13.2);
    private static final VoxelShape SHAPE_VERTICAL_NORTH = box(0, 0, 16 - 13.2, 16, 16, 16);
    private static final VoxelShape SHAPE_VERTICAL_EAST = box(0, 0, 0, 13.2, 16, 16);
    private static final VoxelShape SHAPE_VERTICAL_WEST = box(16 - 13.2, 0, 0, 16, 16, 16);

    private final PortableGridBlockEntityTicker ticker;
    private final BlockEntityProvider<AbstractPortableGridBlockEntity> blockEntityProvider;

    public PortableGridBlock(final PortableGridType type,
                             final BlockEntityProvider<AbstractPortableGridBlockEntity> blockEntityProvider) {
        super(BlockProperties.stone(type == PortableGridType.NORMAL
            ? ContentIds.PORTABLE_GRID
            : ContentIds.CREATIVE_PORTABLE_GRID));
        this.ticker = new PortableGridBlockEntityTicker(() -> type == PortableGridType.NORMAL
            ? BlockEntities.INSTANCE.getPortableGrid()
            : BlockEntities.INSTANCE.getCreativePortableGrid());
        this.blockEntityProvider = blockEntityProvider;
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
    protected DirectionType<OrientedDirection> getDirectionType() {
        return OrientedDirectionType.INSTANCE;
    }

    @Override
    public VoxelShape getShape(final BlockState state,
                               final BlockGetter level,
                               final BlockPos pos,
                               final CollisionContext ctx) {
        final OrientedDirection direction = getDirection(state);
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
        return blockEntityProvider.create(blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(final Level level,
                                                                            final BlockState state,
                                                                            final BlockEntityType<T> type) {
        return ticker.get(level, type);
    }
}
