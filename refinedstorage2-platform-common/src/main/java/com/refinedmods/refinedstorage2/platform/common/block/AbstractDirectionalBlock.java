package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.direction.DirectionType;

import javax.annotation.Nullable;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public abstract class AbstractDirectionalBlock<T extends Enum<T> & StringRepresentable> extends AbstractBaseBlock {
    private final DirectionType<T> directionType;

    protected AbstractDirectionalBlock(final Properties properties, final DirectionType<T> directionType) {
        super(properties);
        this.directionType = directionType;
    }

    protected BlockState getDefaultState() {
        return super.getDefaultState().setValue(directionType.getProperty(), directionType.getDefault());
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(directionType.getProperty());
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext ctx) {
        final BlockState state = defaultBlockState();
        return state.setValue(
            directionType.getProperty(),
            directionType.getDirection(
                ctx.getHorizontalDirection(),
                ctx.getPlayer() != null ? ctx.getPlayer().getXRot() : 0
            )
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(final BlockState state, final Rotation rotation) {
        final EnumProperty<T> directionProperty = directionType.getProperty();
        final T currentDirection = state.getValue(directionProperty);
        return state.setValue(directionProperty, directionType.rotate(currentDirection));
    }

    @Nullable
    public T getDirection(@Nullable final BlockState state) {
        return state != null && state.hasProperty(directionType.getProperty())
            ? state.getValue(directionType.getProperty())
            : null;
    }
}
