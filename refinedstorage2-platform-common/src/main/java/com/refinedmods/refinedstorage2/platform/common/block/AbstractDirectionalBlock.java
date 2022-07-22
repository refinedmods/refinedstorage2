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
    protected AbstractDirectionalBlock(final Properties properties) {
        super(properties);
    }

    protected abstract DirectionType<T> getDirectionType();

    @Override
    protected BlockState getDefaultState() {
        return super.getDefaultState().setValue(getDirectionType().getProperty(), getDirectionType().getDefault());
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(getDirectionType().getProperty());
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext ctx) {
        final BlockState state = defaultBlockState();
        return state.setValue(
            getDirectionType().getProperty(),
            getDirectionType().getDirection(
                ctx.getClickedFace(),
                ctx.getHorizontalDirection(),
                ctx.getPlayer() != null ? ctx.getPlayer().getXRot() : 0
            )
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(final BlockState state, final Rotation rotation) {
        final EnumProperty<T> directionProperty = getDirectionType().getProperty();
        final T currentDirection = state.getValue(directionProperty);
        return state.setValue(directionProperty, getDirectionType().rotate(currentDirection));
    }

    @Nullable
    public T getDirection(@Nullable final BlockState state) {
        final EnumProperty<T> directionProperty = getDirectionType().getProperty();
        return state != null && state.hasProperty(directionProperty)
            ? state.getValue(directionProperty)
            : null;
    }
}
