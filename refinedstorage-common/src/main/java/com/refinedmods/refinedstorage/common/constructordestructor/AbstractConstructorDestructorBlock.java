package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalCableBlock;
import com.refinedmods.refinedstorage.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.common.support.ColorableBlock;
import com.refinedmods.refinedstorage.common.support.DirectionalCableBlockShapes;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
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
import org.jspecify.annotations.Nullable;

public abstract class AbstractConstructorDestructorBlock<T extends Block & BlockItemProvider<I>,
    B extends BlockEntity, I extends BlockItem>
    extends AbstractDirectionalCableBlock
    implements ColorableBlock<T, I>, EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final ConcurrentHashMap<DirectionalCacheShapeCacheKey, VoxelShape> SHAPE_CACHE =
        new ConcurrentHashMap<>();

    private final AbstractBlockEntityTicker<B> ticker;
    private final DyeColor color;
    private final MutableComponent name;

    protected AbstractConstructorDestructorBlock(final Identifier id,
                                                 final DyeColor color,
                                                 final MutableComponent name,
                                                 final AbstractBlockEntityTicker<B> ticker) {
        super(id, SHAPE_CACHE);
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
    public <E extends BlockEntity> BlockEntityTicker<E> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<E> type) {
        return ticker.get(level, type);
    }
}
