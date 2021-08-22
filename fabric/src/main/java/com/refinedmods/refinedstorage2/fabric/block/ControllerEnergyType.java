package com.refinedmods.refinedstorage2.fabric.block;

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
        switch (state) {
            case OFF:
                return OFF;
            case NEARLY_ON:
                return NEARLY_ON;
            case ON:
                return ON;
            case NEARLY_OFF:
                return NEARLY_OFF;
            default:
                return OFF;
        }
    }
}
