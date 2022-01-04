package com.refinedmods.refinedstorage2.platform.forge.internal;

import com.refinedmods.refinedstorage2.platform.abstractions.Config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigImpl implements Config {
    private final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    private final ForgeConfigSpec spec;

    private final CableImpl cable;
    private final ControllerImpl controller;

    public ConfigImpl() {
        cable = new CableImpl();
        controller = new ControllerImpl();
        spec = builder.build();
    }

    public ForgeConfigSpec getSpec() {
        return spec;
    }

    @Override
    public Grid getGrid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Controller getController() {
        return controller;
    }

    @Override
    public DiskDrive getDiskDrive() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Cable getCable() {
        return cable;
    }

    private class CableImpl implements Cable {
        private final ForgeConfigSpec.LongValue usage;

        private CableImpl() {
            builder.push("cable");
            usage = builder.comment("The energy used by the Cable").defineInRange("usage", 0, 0L, Long.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyUsage() {
            return usage.get();
        }
    }

    private class ControllerImpl implements Controller {
        private final ForgeConfigSpec.IntValue capacity;

        private ControllerImpl() {
            builder.push("controller");
            capacity = builder.comment("The energy capacity of the Controller").defineInRange("capacity", 1000, 0, Integer.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getCapacity() {
            return capacity.get();
        }
    }
}
