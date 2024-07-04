package com.refinedmods.refinedstorage.platform.common.support.direction;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class HorizontalDirectionType implements DirectionType<HorizontalDirection> {
    public static final DirectionType<HorizontalDirection> INSTANCE = new HorizontalDirectionType();

    private static final EnumProperty<HorizontalDirection> PROPERTY = EnumProperty.create(
        "direction",
        HorizontalDirection.class
    );

    @Override
    public EnumProperty<HorizontalDirection> getProperty() {
        return PROPERTY;
    }

    @Override
    public HorizontalDirection getDefault() {
        return HorizontalDirection.NORTH;
    }

    @Override
    public Direction extractDirection(final HorizontalDirection direction) {
        return switch (direction) {
            case NORTH -> Direction.NORTH;
            case EAST -> Direction.EAST;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
        };
    }

    @Override
    public HorizontalDirection getDirection(final Direction clickedFace,
                                            final Direction playerFacing,
                                            final float playerPitch) {
        return switch (playerFacing) {
            case EAST -> HorizontalDirection.EAST;
            case SOUTH -> HorizontalDirection.SOUTH;
            case WEST -> HorizontalDirection.WEST;
            default -> HorizontalDirection.NORTH;
        };
    }

    @Override
    public HorizontalDirection rotate(final HorizontalDirection direction) {
        return switch (direction) {
            case NORTH -> HorizontalDirection.EAST;
            case EAST -> HorizontalDirection.SOUTH;
            case SOUTH -> HorizontalDirection.WEST;
            case WEST -> HorizontalDirection.NORTH;
        };
    }
}
