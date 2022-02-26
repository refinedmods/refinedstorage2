package com.refinedmods.refinedstorage2.platform.fabric.internal;

import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = IdentifierUtil.MOD_ID)
public class ConfigImpl implements ConfigData, com.refinedmods.refinedstorage2.platform.abstractions.Config {
    @ConfigEntry.Gui.CollapsibleObject
    private GridImpl grid = new GridImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private ControllerImpl controller = new ControllerImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private DiskDriveImpl diskDrive = new DiskDriveImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private CableImpl cable = new CableImpl();

    public static ConfigImpl get() {
        return AutoConfig.getConfigHolder(ConfigImpl.class).getConfig();
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

    private static class GridImpl implements Grid {
        private boolean largeFont = false;

        private long energyUsage = 10;

        @ConfigEntry.BoundedDiscrete(min = 3L, max = 256)
        private int maxRowsStretch = 256;

        private boolean preventSortingWhileShiftIsDown = true;

        private boolean detailedTooltip = true;

        private boolean rememberSearchQuery = false;

        private boolean smoothScrolling = true;

        private boolean autoSelected = false;

        @Override
        public boolean isLargeFont() {
            return largeFont;
        }

        @Override
        public int getMaxRowsStretch() {
            return maxRowsStretch;
        }

        @Override
        public boolean isPreventSortingWhileShiftIsDown() {
            return preventSortingWhileShiftIsDown;
        }

        @Override
        public boolean isDetailedTooltip() {
            return detailedTooltip;
        }

        @Override
        public boolean isRememberSearchQuery() {
            return rememberSearchQuery;
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public boolean isSmoothScrolling() {
            return smoothScrolling;
        }

        @Override
        public boolean isAutoSelected() {
            return autoSelected;
        }
    }

    private static class DiskDriveImpl implements DiskDrive {
        private long energyUsage = 10;

        private long energyUsagePerDisk = 5;

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public long getEnergyUsagePerDisk() {
            return energyUsagePerDisk;
        }
    }

    private static class CableImpl implements Cable {
        private long energyUsage = 0;

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }
    }

    private static class ControllerImpl implements Controller {
        private long energyCapacity = 1000;

        public long getEnergyCapacity() {
            return energyCapacity;
        }
    }
}
