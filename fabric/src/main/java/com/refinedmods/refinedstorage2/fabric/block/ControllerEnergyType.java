package com.refinedmods.refinedstorage2.fabric.block;

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
}
