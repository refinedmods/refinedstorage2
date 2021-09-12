package com.refinedmods.refinedstorage2.platform.fabric.util;

import java.util.Locale;

import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

public enum BiDirection implements StringIdentifiable {
    NORTH(new Vec3f(0, 0, 0)),
    EAST(new Vec3f(0, -90, 0)),
    SOUTH(new Vec3f(0, 180, 0)),
    WEST(new Vec3f(0, 90, 0)),
    UP_NORTH(new Vec3f(90, 0, 180)),
    UP_EAST(new Vec3f(90, 0, -90)),
    UP_SOUTH(new Vec3f(90, 0, 0)),
    UP_WEST(new Vec3f(90, 0, 90)),
    DOWN_NORTH(new Vec3f(-90, 0, 0)),
    DOWN_EAST(new Vec3f(-90, 0, -90)),
    DOWN_SOUTH(new Vec3f(-90, 0, 180)),
    DOWN_WEST(new Vec3f(-90, 0, 90));

    private final Vec3f vec;

    BiDirection(Vec3f vec) {
        this.vec = vec;
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

    public static BiDirection forHorizontal(Direction horizontalDirection) {
        return switch (horizontalDirection) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            default -> throw new IllegalArgumentException(String.valueOf(horizontalDirection));
        };
    }

    public static BiDirection forUp(Direction verticalDirection) {
        return switch (verticalDirection) {
            case NORTH -> UP_NORTH;
            case SOUTH -> UP_SOUTH;
            case WEST -> UP_WEST;
            case EAST -> UP_EAST;
            default -> throw new IllegalArgumentException(String.valueOf(verticalDirection));
        };
    }

    public static BiDirection forDown(Direction verticalDirection) {
        return switch (verticalDirection) {
            case NORTH -> DOWN_NORTH;
            case SOUTH -> DOWN_SOUTH;
            case WEST -> DOWN_WEST;
            case EAST -> DOWN_EAST;
            default -> throw new IllegalArgumentException(String.valueOf(verticalDirection));
        };
    }

    public Vec3f getVec() {
        return vec;
    }

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
