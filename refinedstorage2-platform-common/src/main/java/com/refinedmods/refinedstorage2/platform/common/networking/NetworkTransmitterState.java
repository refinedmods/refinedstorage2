package com.refinedmods.refinedstorage2.platform.common.networking;

import net.minecraft.util.StringRepresentable;

public enum NetworkTransmitterState implements StringRepresentable {
    ACTIVE("active"),
    ERROR("error"),
    INACTIVE("inactive");

    private final String name;

    NetworkTransmitterState(final String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
