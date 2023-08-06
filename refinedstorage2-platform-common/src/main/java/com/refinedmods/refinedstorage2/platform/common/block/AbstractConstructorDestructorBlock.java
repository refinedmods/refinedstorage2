package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.ticker.AbstractBlockEntityTicker;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

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

public abstract class AbstractConstructorDestructorBlock<T extends Block & BlockItemProvider, B extends BlockEntity>
    extends AbstractDirectionalCableBlock
    implements ColorableBlock<T>, EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final Map<DirectionalCacheShapeCacheKey, VoxelShape> SHAPE_CACHE = new HashMap<>();

    private final AbstractBlockEntityTicker<B> ticker;
    private final DyeColor color;
    private final MutableComponent name;

    protected AbstractConstructorDestructorBlock(final DyeColor color,
                                                 final MutableComponent name,
                                                 final AbstractBlockEntityTicker<B> ticker) {
        super(SHAPE_CACHE);
        this.color = color;
        this.name = name;
        this.ticker = ticker;
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
            case NORTH -> DirectionalCableBlockShapes.CONSTRUCTOR_DESTRUCTOR_NORTH;
            case EAST -> DirectionalCableBlockShapes.CONSTRUCTOR_DESTRUCTOR_EAST;
            case SOUTH -> DirectionalCableBlockShapes.CONSTRUCTOR_DESTRUCTOR_SOUTH;
            case WEST -> DirectionalCableBlockShapes.CONSTRUCTOR_DESTRUCTOR_WEST;
            case UP -> DirectionalCableBlockShapes.CONSTRUCTOR_DESTRUCTOR_UP;
            case DOWN -> DirectionalCableBlockShapes.CONSTRUCTOR_DESTRUCTOR_DOWN;
        };
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
    public <I extends BlockEntity> BlockEntityTicker<I> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<I> type) {
        return ticker.get(level, type);
    }
}
