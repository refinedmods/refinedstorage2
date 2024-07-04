package com.refinedmods.refinedstorage.platform.common.support.direction;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class DefaultDirectionType implements DirectionType<Direction> {
    public static final DirectionType<Direction> FACE_CLICKED = new DefaultDirectionType(false);
    public static final DirectionType<Direction> FACE_PLAYER = new DefaultDirectionType(true);

    private static final EnumProperty<Direction> PROPERTY = EnumProperty.create("direction", Direction.class);

    private final boolean facePlayer;

    private DefaultDirectionType(final boolean facePlayer) {
        this.facePlayer = facePlayer;
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
        if (facePlayer) {
            if (playerPitch > 65) {
                return Direction.UP;
            } else if (playerPitch < -65) {
                return Direction.DOWN;
            }
            return playerFacing.getOpposite();
        }
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
