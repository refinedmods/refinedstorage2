package com.refinedmods.refinedstorage2.api.core;

import java.util.Objects;

public record Position(int x, int y, int z) implements Comparable<Position> {
    public static final Position ORIGIN = new Position(0, 0, 0);

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

    public Position south() {
        return offset(Direction.SOUTH);
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

    @Override
    public int compareTo(Position o) {
        if (this.getY() == o.getY()) {
            return this.getZ() == o.getZ() ? this.getX() - o.getX() : this.getZ() - o.getZ();
        } else {
            return this.getY() - o.getY();
        }
    }
}
