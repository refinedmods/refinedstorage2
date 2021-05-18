package com.refinedmods.refinedstorage2.fabric.util;

import java.util.Locale;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;

public enum BiDirection implements StringIdentifiable {
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

    BiDirection(Vector3f vec) {
        this.vec = vec;
    }

    public static BiDirection forHorizontal(Direction horizontalDirection) {
        switch (horizontalDirection) {
            case NORTH:
                return NORTH;
            case SOUTH:
                return SOUTH;
            case WEST:
                return WEST;
            case EAST:
                return EAST;
            default:
                throw new IllegalArgumentException("Invalid: " + horizontalDirection);
        }
    }

    public static BiDirection forUp(Direction verticalDirection) {
        switch (verticalDirection) {
            case NORTH:
                return UP_NORTH;
            case SOUTH:
                return UP_SOUTH;
            case WEST:
                return UP_WEST;
            case EAST:
                return UP_EAST;
            default:
                throw new IllegalArgumentException("Invalid: " + verticalDirection);
        }
    }

    public static BiDirection forDown(Direction verticalDirection) {
        switch (verticalDirection) {
            case NORTH:
                return DOWN_NORTH;
            case SOUTH:
                return DOWN_SOUTH;
            case WEST:
                return DOWN_WEST;
            case EAST:
                return DOWN_EAST;
            default:
                throw new IllegalArgumentException("Invalid: " + verticalDirection);
        }
    }

    public Vector3f getVec() {
        return vec;
    }

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
