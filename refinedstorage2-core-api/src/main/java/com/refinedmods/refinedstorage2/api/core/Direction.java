package com.refinedmods.refinedstorage2.api.core;

public enum Direction {
    DOWN(new Position(0, -1, 0), 1),
    UP(new Position(0, 1, 0), 0),
    NORTH(new Position(0, 0, -1), 3),
    SOUTH(new Position(0, 0, 1), 2),
    WEST(new Position(-1, 0, 0), 5),
    EAST(new Position(1, 0, 0), 4);

    private final Position position;
    private final int oppositeIdx;

    Direction(Position position, int oppositeIdx) {
        this.position = position;
        this.oppositeIdx = oppositeIdx;
    }

    public Position getPosition() {
        return position;
    }

    public Direction getOpposite() {
        return Direction.values()[oppositeIdx];
    }
}
