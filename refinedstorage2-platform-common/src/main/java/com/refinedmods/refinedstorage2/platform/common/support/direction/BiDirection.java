package com.refinedmods.refinedstorage2.platform.common.support.direction;

import java.util.Locale;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public enum BiDirection implements StringRepresentable {
    NORTH(new Vector3f(0, 0, 0), Direction.NORTH),
    EAST(new Vector3f(0, -90, 0), Direction.EAST),
    SOUTH(new Vector3f(0, 180, 0), Direction.SOUTH),
    WEST(new Vector3f(0, 90, 0), Direction.WEST),
    UP_NORTH(new Vector3f(90, 0, 180), Direction.UP),
    UP_EAST(new Vector3f(90, 0, -90), Direction.UP),
    UP_SOUTH(new Vector3f(90, 0, 0), Direction.UP),
    UP_WEST(new Vector3f(90, 0, 90), Direction.UP),
    DOWN_NORTH(new Vector3f(-90, 0, 0), Direction.DOWN),
    DOWN_EAST(new Vector3f(-90, 0, -90), Direction.DOWN),
    DOWN_SOUTH(new Vector3f(-90, 0, 180), Direction.DOWN),
    DOWN_WEST(new Vector3f(-90, 0, 90), Direction.DOWN);

    private final Vector3f vec;
    private final String name;
    private final Quaternionf quaternion;
    private final Direction direction;

    BiDirection(final Vector3f vec, final Direction direction) {
        this.vec = vec;
        this.name = name().toLowerCase(Locale.ROOT);
        this.quaternion = new Quaternionf().rotateXYZ(
            vec.x() * Mth.DEG_TO_RAD,
            vec.y() * Mth.DEG_TO_RAD,
            vec.z() * Mth.DEG_TO_RAD
        );
        this.direction = direction;
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

    public Quaternionf getQuaternion() {
        return quaternion;
    }

    public Direction asDirection() {
        return direction;
    }
}
