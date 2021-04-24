package com.refinedmods.refinedstorage2.core.util;

public enum Direction {
    DOWN(new Position(0, -1, 0)),
    UP(new Position(0, 1, 0)),
    NORTH(new Position(0, 0, -1)),
    SOUTH(new Position(0, 0, 1)),
    WEST(new Position(-1, 0, 0)),
    EAST(new Position(1, 0, 0));

    private final Position position;

    Direction(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }
}
