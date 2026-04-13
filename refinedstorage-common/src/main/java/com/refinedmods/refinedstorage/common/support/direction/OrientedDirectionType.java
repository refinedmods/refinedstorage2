package com.refinedmods.refinedstorage.common.support.direction;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class OrientedDirectionType implements DirectionType<OrientedDirection> {
    public static final DirectionType<OrientedDirection> INSTANCE = new OrientedDirectionType();

    private static final EnumProperty<OrientedDirection> PROPERTY = EnumProperty.create("direction",
        OrientedDirection.class);

    private OrientedDirectionType() {
    }

    @Override
    public EnumProperty<OrientedDirection> getProperty() {
        return PROPERTY;
    }

    @Override
    public OrientedDirection getDefault() {
        return OrientedDirection.NORTH;
    }

    @Override
    public Direction extractDirection(final OrientedDirection direction) {
        return switch (direction) {
            case NORTH -> Direction.NORTH;
            case EAST -> Direction.EAST;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
            case UP_NORTH, UP_EAST, UP_SOUTH, UP_WEST -> Direction.UP;
            case DOWN_NORTH, DOWN_EAST, DOWN_SOUTH, DOWN_WEST -> Direction.DOWN;
        };
    }

    @Override
    public OrientedDirection getDirection(final Direction clickedFace,
                                          final Direction playerFacing,
                                          final float playerPitch) {
        if (playerPitch > 65) {
            return OrientedDirection.forUpDirection(playerFacing);
        } else if (playerPitch < -65) {
            return OrientedDirection.forDownDirection(playerFacing.getOpposite());
        } else {
            return OrientedDirection.forHorizontalDirection(playerFacing.getOpposite());
        }
    }

    @Override
    public OrientedDirection rotate(final OrientedDirection direction) {
        return direction.rotate();
    }
}
