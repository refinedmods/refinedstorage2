package com.refinedmods.refinedstorage2.platform.fabric.block;

import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerEnergyState;

import net.minecraft.util.StringIdentifiable;

public enum ControllerEnergyType implements StringIdentifiable {
    OFF("off"),
    NEARLY_OFF("nearly_off"),
    NEARLY_ON("nearly_on"),
    ON("on");

    private final String name;

    ControllerEnergyType(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }

    public static ControllerEnergyType ofState(ControllerEnergyState state) {
        return switch (state) {
            case OFF -> OFF;
            case NEARLY_ON -> NEARLY_ON;
            case ON -> ON;
            case NEARLY_OFF -> NEARLY_OFF;
        };
    }
}
