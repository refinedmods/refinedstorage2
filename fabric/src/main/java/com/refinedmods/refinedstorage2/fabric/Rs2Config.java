package com.refinedmods.refinedstorage2.fabric;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;

@Config(name = Rs2Mod.ID)
public class Rs2Config implements ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    private Grid grid = new Grid();

    @ConfigEntry.Gui.CollapsibleObject
    private Controller controller = new Controller();

    @ConfigEntry.Gui.CollapsibleObject
    private DiskDrive diskDrive = new DiskDrive();

    @ConfigEntry.Gui.CollapsibleObject
    private Cable cable = new Cable();

    public static Rs2Config get() {
        return AutoConfig.getConfigHolder(Rs2Config.class).getConfig();
    }

    public Grid getGrid() {
        return grid;
    }

    public Controller getController() {
        return controller;
    }

    public DiskDrive getDiskDrive() {
        return diskDrive;
    }

    public Cable getCable() {
        return cable;
    }

    public static class Grid {
        private boolean largeFont = false;

        private long energyUsage = 100;

        @ConfigEntry.BoundedDiscrete(min = 3L, max = 256)
        private int maxRowsStretch = 256;

        private boolean preventSortingWhileShiftIsDown = true;

        private boolean detailedTooltip = true;

        private boolean rememberSearchQuery = false;

        public boolean isLargeFont() {
            return largeFont;
        }

        public int getMaxRowsStretch() {
            return maxRowsStretch;
        }

        public boolean isPreventSortingWhileShiftIsDown() {
            return preventSortingWhileShiftIsDown;
        }

        public boolean isDetailedTooltip() {
            return detailedTooltip;
        }

        public boolean isRememberSearchQuery() {
            return rememberSearchQuery;
        }

        public long getEnergyUsage() {
            return energyUsage;
        }
    }

    public static class DiskDrive {
        private long energyUsage = 300;
        private long energyUsagePerDisk = 10;

        public long getEnergyUsage() {
            return energyUsage;
        }

        public long getEnergyUsagePerDisk() {
            return energyUsagePerDisk;
        }
    }

    public static class Cable {
        private long energyUsage = 0;

        public long getEnergyUsage() {
            return energyUsage;
        }
    }

    public static class Controller {
        private long capacity = 500;

        public long getCapacity() {
            return capacity;
        }
    }
}
