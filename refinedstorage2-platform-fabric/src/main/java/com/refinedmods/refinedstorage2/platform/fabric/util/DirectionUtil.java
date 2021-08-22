package com.refinedmods.refinedstorage2.platform.fabric.util;

import com.refinedmods.refinedstorage2.api.core.Direction;

public class DirectionUtil {
    private DirectionUtil() {

    }

    public static Direction toDirection(net.minecraft.util.math.Direction direction) {
        switch (direction) {
            case DOWN:
                return Direction.DOWN;
            case UP:
                return Direction.UP;
            case NORTH:
                return Direction.NORTH;
            case SOUTH:
                return Direction.SOUTH;
            case WEST:
                return Direction.WEST;
            case EAST:
                return Direction.EAST;
        }
        throw new IllegalArgumentException(direction.toString());
    }
}
