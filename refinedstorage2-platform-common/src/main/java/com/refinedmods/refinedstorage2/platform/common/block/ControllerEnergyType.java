package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerEnergyState;

import net.minecraft.util.StringRepresentable;

public enum ControllerEnergyType implements StringRepresentable {
    OFF("off"),
    NEARLY_OFF("nearly_off"),
    NEARLY_ON("nearly_on"),
    ON("on");

    private final String name;

    ControllerEnergyType(final String name) {
        this.name = name;
    }

    public static ControllerEnergyType ofState(final ControllerEnergyState state) {
        return switch (state) {
            case OFF -> OFF;
            case NEARLY_ON -> NEARLY_ON;
            case ON -> ON;
            case NEARLY_OFF -> NEARLY_OFF;
        };
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
