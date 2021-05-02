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

    public static Rs2Config get() {
        return AutoConfig.getConfigHolder(Rs2Config.class).getConfig();
    }

    public Grid getGrid() {
        return grid;
    }

    public Controller getController() {
        return controller;
    }

    public static class Grid {
        private boolean largeFont = false;

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
    }

    public static class Controller {
        private long capacity = 500;

        public long getCapacity() {
            return capacity;
        }
    }
}
