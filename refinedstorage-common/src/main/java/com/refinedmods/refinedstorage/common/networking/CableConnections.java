package com.refinedmods.refinedstorage.common.networking;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public record CableConnections(boolean north, boolean east, boolean south, boolean west, boolean up, boolean down) {
    public static final CableConnections NONE = new CableConnections(false, false, false, false, false, false);

    private static final String TAG_NORTH = "North";
    private static final String TAG_EAST = "East";
    private static final String TAG_SOUTH = "South";
    private static final String TAG_WEST = "West";
    private static final String TAG_UP = "Up";
    private static final String TAG_DOWN = "Down";

    public boolean isConnected(final Direction direction) {
        return switch (direction) {
            case NORTH -> north;
            case EAST -> east;
            case SOUTH -> south;
            case WEST -> west;
            case UP -> up;
            case DOWN -> down;
        };
    }

    public static CableConnections load(final ValueInput input) {
        return new CableConnections(
            input.getBooleanOr(TAG_NORTH, false),
            input.getBooleanOr(TAG_EAST, false),
            input.getBooleanOr(TAG_SOUTH, false),
            input.getBooleanOr(TAG_WEST, false),
            input.getBooleanOr(TAG_UP, false),
            input.getBooleanOr(TAG_DOWN, false)
        );
    }

    public void store(final ValueOutput output) {
        output.putBoolean(TAG_NORTH, north);
        output.putBoolean(TAG_EAST, east);
        output.putBoolean(TAG_SOUTH, south);
        output.putBoolean(TAG_WEST, west);
        output.putBoolean(TAG_UP, up);
        output.putBoolean(TAG_DOWN, down);
    }

    public void store(final CompoundTag tag) {
        tag.putBoolean(TAG_NORTH, north);
        tag.putBoolean(TAG_EAST, east);
        tag.putBoolean(TAG_SOUTH, south);
        tag.putBoolean(TAG_WEST, west);
        tag.putBoolean(TAG_UP, up);
        tag.putBoolean(TAG_DOWN, down);
    }

    public static void stripTag(final CompoundTag tag) {
        tag.remove(TAG_DOWN);
        tag.remove(TAG_UP);
        tag.remove(TAG_WEST);
        tag.remove(TAG_NORTH);
        tag.remove(TAG_SOUTH);
        tag.remove(TAG_EAST);
    }
}
