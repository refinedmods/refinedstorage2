package com.refinedmods.refinedstorage2.platform.forge.internal;

import com.refinedmods.refinedstorage2.platform.abstractions.Config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigImpl implements Config {
    private static final String ENERGY_USAGE = "energyUsage";

    private final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    private final ForgeConfigSpec spec;

    private final CableImpl cable;
    private final ControllerImpl controller;
    private final DiskDriveImpl diskDrive;
    private final Grid grid;

    public ConfigImpl() {
        cable = new CableImpl();
        controller = new ControllerImpl();
        diskDrive = new DiskDriveImpl();
        grid = new GridImpl();
        spec = builder.build();
    }

    public ForgeConfigSpec getSpec() {
        return spec;
    }

    @Override
    public Grid getGrid() {
        return grid;
    }

    @Override
    public Controller getController() {
        return controller;
    }

    @Override
    public DiskDrive getDiskDrive() {
        return diskDrive;
    }

    @Override
    public Cable getCable() {
        return cable;
    }

    private class CableImpl implements Cable {
        private final ForgeConfigSpec.LongValue energyUsage;

        private CableImpl() {
            builder.push("cable");
            energyUsage = builder.comment("The energy used by the Cable").defineInRange(ENERGY_USAGE, 0, 0L, Long.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage.get();
        }
    }

    private class ControllerImpl implements Controller {
        private final ForgeConfigSpec.IntValue energyCapacity;

        private ControllerImpl() {
            builder.push("controller");
            energyCapacity = builder.comment("The energy capacity of the Controller").defineInRange("energyCapacity", 1000, 0, Integer.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyCapacity() {
            return energyCapacity.get();
        }
    }

    private class DiskDriveImpl implements DiskDrive {
        private final ForgeConfigSpec.IntValue energyUsage;
        private final ForgeConfigSpec.IntValue energyUsagePerDisk;

        private DiskDriveImpl() {
            builder.push("diskDrive");
            energyUsage = builder.comment("The energy used by the Disk Drive").defineInRange(ENERGY_USAGE, 10, 0, Integer.MAX_VALUE);
            energyUsagePerDisk = builder.comment("The energy used per disk").defineInRange("energyUsagePerDisk", 5, 0, Integer.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage.get();
        }

        @Override
        public long getEnergyUsagePerDisk() {
            return energyUsagePerDisk.get();
        }
    }

    private class GridImpl implements Grid {
        private final ForgeConfigSpec.BooleanValue largeFont;
        private final ForgeConfigSpec.IntValue maxRowsStretch;
        private final ForgeConfigSpec.BooleanValue preventSortingWhileShiftIsDown;
        private final ForgeConfigSpec.BooleanValue detailedTooltip;
        private final ForgeConfigSpec.BooleanValue rememberSearchQuery;
        private final ForgeConfigSpec.IntValue energyUsage;
        private final ForgeConfigSpec.BooleanValue smoothScrolling;
        private final ForgeConfigSpec.BooleanValue autoSelected;

        public GridImpl() {
            builder.push("grid");
            largeFont = builder.comment("Whether the Grid should use a large font for quantities").define("largeFont", false);
            maxRowsStretch = builder.comment("The maximum amount of rows that can be displayed when the Grid is in stretch view mode").defineInRange("maxRowsStretch", 256, 3, 256);
            preventSortingWhileShiftIsDown = builder.comment("Whether the Grid should avoid sorting when shift is held down").define("preventSortingWhileShiftIsDown", true);
            detailedTooltip = builder.comment("Whether the Grid should show a detailed tooltip").define("detailedTooltip", true);
            rememberSearchQuery = builder.comment("Whether the search query should persist when closing and re-opening the Grid").define("rememberSearchQuery", false);
            energyUsage = builder.comment("The energy used by the Grid").defineInRange(ENERGY_USAGE, 10, 0, Integer.MAX_VALUE);
            smoothScrolling = builder.comment("Whether the Grid should use smooth scrolling").define("smoothScrolling", true);
            autoSelected = builder.comment("Whether the Grid search box is auto selected").define("autoSelected", false);
            builder.pop();
        }

        @Override
        public boolean isLargeFont() {
            return largeFont.get();
        }

        @Override
        public int getMaxRowsStretch() {
            return maxRowsStretch.get();
        }

        @Override
        public boolean isPreventSortingWhileShiftIsDown() {
            return preventSortingWhileShiftIsDown.get();
        }

        @Override
        public boolean isDetailedTooltip() {
            return detailedTooltip.get();
        }

        @Override
        public boolean isRememberSearchQuery() {
            return rememberSearchQuery.get();
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage.get();
        }

        @Override
        public boolean isSmoothScrolling() {
            return smoothScrolling.get();
        }

        @Override
        public boolean isAutoSelected() {
            return autoSelected.get();
        }

        @Override
        public void setAutoSelected(boolean autoSelected) {
            this.autoSelected.set(autoSelected);
        }
    }
}
