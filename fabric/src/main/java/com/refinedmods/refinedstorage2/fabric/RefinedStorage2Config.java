package com.refinedmods.refinedstorage2.fabric;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;

@Config(name = RefinedStorage2Mod.ID)
public class RefinedStorage2Config implements ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    private Grid grid = new Grid();

    public static class Grid {
        private boolean largeFont = false;

        @ConfigEntry.BoundedDiscrete(min = 3L, max = 256)
        private int maxRowsStretch = 256;

        public boolean isLargeFont() {
            return largeFont;
        }

        public int getMaxRowsStretch() {
            return maxRowsStretch;
        }
    }

    public Grid getGrid() {
        return grid;
    }

    public static RefinedStorage2Config get() {
        return AutoConfig.getConfigHolder(RefinedStorage2Config.class).getConfig();
    }
}
