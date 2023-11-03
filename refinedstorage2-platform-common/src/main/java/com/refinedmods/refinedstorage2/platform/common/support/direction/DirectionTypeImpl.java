package com.refinedmods.refinedstorage2.platform.common.support.direction;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class DirectionTypeImpl implements DirectionType<Direction> {
    public static final DirectionType<Direction> INSTANCE = new DirectionTypeImpl();

    private static final EnumProperty<Direction> PROPERTY = EnumProperty.create("direction", Direction.class);

    private DirectionTypeImpl() {
    }

    @Override
    public EnumProperty<Direction> getProperty() {
        return PROPERTY;
    }

    @Override
    public Direction getDefault() {
        return Direction.NORTH;
    }

    @Override
    public Direction extractDirection(final Direction direction) {
        return direction;
    }

    @Override
    public Direction getDirection(final Direction clickedFace, final Direction playerFacing, final float playerPitch) {
        return clickedFace.getOpposite();
    }

    @Override
    public Direction rotate(final Direction direction) {
        final Direction[] directions = Direction.values();
        return direction.ordinal() + 1 >= directions.length
            ? directions[0]
            : directions[direction.ordinal() + 1];
    }
}
