package com.refinedmods.refinedstorage2.fabric.util;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3i;

import java.util.Locale;

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

    private final Quaternion quaternion;
    private final Matrix4f mat;

    BiDirection(Vector3f vec) {
        this.quaternion = new Quaternion(vec.getX(), vec.getY(), vec.getZ(), true);
        this.mat = new Matrix4f();
        this.mat.loadIdentity();
        this.mat.multiply(quaternion);
    }

    public Quaternion getQuaternion() {
        return quaternion;
    }

    public Direction rotate(Direction facing) {
        Vec3i dir = facing.getVector();
        Vector4f vec = new Vector4f((float) dir.getX(), (float) dir.getY(), (float) dir.getZ(), 1.0F);
        vec.transform(mat);
        return Direction.getFacing(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
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
}
