package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.destructor.DestructorBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.NetworkNodeBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DestructorBlock extends AbstractDirectionalCableBlock
    implements ColorableBlock<DestructorBlock>, EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final Map<DirectionalCacheShapeCacheKey, VoxelShape> SHAPE_CACHE = new HashMap<>();
    private static final AbstractBlockEntityTicker<DestructorBlockEntity> TICKER = new NetworkNodeBlockEntityTicker<>(
        BlockEntities.INSTANCE::getDestructor,
        ACTIVE
    );
    private final DyeColor color;
    private final MutableComponent name;

    public DestructorBlock(final DyeColor color, final MutableComponent name) {
        super(SHAPE_CACHE);
        this.color = color;
        this.name = name;
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
    protected VoxelShape getExtensionShape(final Direction direction) {
        return switch (direction) {
            case NORTH -> DirectionalCableBlockShapes.DESTRUCTOR_NORTH;
            case EAST -> DirectionalCableBlockShapes.DESTRUCTOR_EAST;
            case SOUTH -> DirectionalCableBlockShapes.DESTRUCTOR_SOUTH;
            case WEST -> DirectionalCableBlockShapes.DESTRUCTOR_WEST;
            case UP -> DirectionalCableBlockShapes.DESTRUCTOR_UP;
            case DOWN -> DirectionalCableBlockShapes.DESTRUCTOR_DOWN;
        };
    }

    @Override
    public BlockColorMap<DestructorBlock> getBlockColorMap() {
        return Blocks.INSTANCE.getDestructor();
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override
    public MutableComponent getName() {
        return name;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        return new DestructorBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<T> type) {
        return TICKER.get(level, type);
    }
}
