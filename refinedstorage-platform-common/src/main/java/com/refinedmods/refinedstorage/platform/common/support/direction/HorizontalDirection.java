package com.refinedmods.refinedstorage.platform.common.support.direction;

import java.util.Locale;

import net.minecraft.util.StringRepresentable;

public enum HorizontalDirection implements StringRepresentable {
    NORTH, EAST, SOUTH, WEST;

    private final String name;

    HorizontalDirection() {
        this.name = name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
