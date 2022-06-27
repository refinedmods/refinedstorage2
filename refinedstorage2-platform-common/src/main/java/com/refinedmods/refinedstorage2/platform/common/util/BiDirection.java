package com.refinedmods.refinedstorage2.platform.common.util;

import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum BiDirection implements StringRepresentable {
    NORTH(new Vector3f(0, 0, 0)),
    EAST(new Vector3f(0, -90, 0)),
    SOUTH(new Vector3f(0, 180, 0)),
    WEST(new Vector3f(0, 90, 0)),
    UP_NORTH(new Vector3f(90, 0, 180)),
    UP_EAST(new Vector3f(90, 0, -90)),
    UP_SOUTH(new Vector3f(90, 0, 0)),
    UP_WEST(new Vector3f(90, 0, 90)),
    DOWN_NORTH(new Vector3f(-90, 0, 0)),
    DOWN_EAST(new Vector3f(-90, 0, -90)),
    DOWN_SOUTH(new Vector3f(-90, 0, 180)),
    DOWN_WEST(new Vector3f(-90, 0, 90));

    private final Vector3f vec;
    private final String name;

    BiDirection(final Vector3f vec) {
        this.vec = vec;
        this.name = name().toLowerCase(Locale.ROOT);
    }

    public static BiDirection forHorizontal(final Direction horizontalDirection) {
        return switch (horizontalDirection) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            default -> throw new IllegalArgumentException(String.valueOf(horizontalDirection));
        };
    }

    public static BiDirection forUp(final Direction verticalDirection) {
        return switch (verticalDirection) {
            case NORTH -> UP_NORTH;
            case SOUTH -> UP_SOUTH;
            case WEST -> UP_WEST;
            case EAST -> UP_EAST;
            default -> throw new IllegalArgumentException(String.valueOf(verticalDirection));
        };
    }

    public static BiDirection forDown(final Direction verticalDirection) {
        return switch (verticalDirection) {
            case NORTH -> DOWN_NORTH;
            case SOUTH -> DOWN_SOUTH;
            case WEST -> DOWN_WEST;
            case EAST -> DOWN_EAST;
            default -> throw new IllegalArgumentException(String.valueOf(verticalDirection));
        };
    }

    public BiDirection rotate() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> UP_NORTH;
            case UP_NORTH -> UP_EAST;
            case UP_EAST -> UP_SOUTH;
            case UP_SOUTH -> UP_WEST;
            case UP_WEST -> DOWN_NORTH;
            case DOWN_NORTH -> DOWN_EAST;
            case DOWN_EAST -> DOWN_SOUTH;
            case DOWN_SOUTH -> DOWN_WEST;
            case DOWN_WEST -> NORTH;
        };
    }

    public Vector3f getVec() {
        return vec;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
