package com.refinedmods.refinedstorage2.core.util;

import java.util.Objects;

public final class Position {
    public static final Position ORIGIN = new Position(0, 0, 0);

    private final int x;
    private final int y;
    private final int z;

    public Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Position offset(Direction direction) {
        return offset(direction.getPosition());
    }

    private Position offset(Position position) {
        return new Position(x + position.getX(), y + position.getY(), z + position.getZ());
    }

    public Position down() {
        return offset(Direction.DOWN);
    }

    public Position north() {
        return offset(Direction.NORTH);
    }

    public Position east() {
        return offset(Direction.EAST);
    }

    public Position up() {
        return offset(Direction.UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y && z == position.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
