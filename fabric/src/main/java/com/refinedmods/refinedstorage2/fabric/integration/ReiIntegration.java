package com.refinedmods.refinedstorage2.fabric.integration;

import com.refinedmods.refinedstorage2.core.grid.query.GridQueryParser;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.coreimpl.grid.ReiGridSearchBoxMode;
import com.refinedmods.refinedstorage2.fabric.integration.rei.ReiProxy;

public class ReiIntegration {
    private static Boolean loaded;

    private ReiIntegration() {
    }

    public static boolean isLoaded() {
        if (loaded == null) {
            try {
                Class.forName("me.shedaniel.rei.api.REIHelper");
                loaded = true;
            } catch (ClassNotFoundException e) {
                loaded = false;
            }
        }
        return loaded;
    }

    public static void registerGridSearchBoxModes(GridQueryParser queryParser) {
        ReiProxy reiProxy = new ReiProxy();

        Rs2Mod.API.getGridSearchBoxModeRegistry().add(ReiGridSearchBoxMode.create(queryParser, false, false, reiProxy)); // REI
        Rs2Mod.API.getGridSearchBoxModeRegistry().add(ReiGridSearchBoxMode.create(queryParser, true, false, reiProxy)); // REI autoselected

        Rs2Mod.API.getGridSearchBoxModeRegistry().add(ReiGridSearchBoxMode.create(queryParser, false, true, reiProxy)); // REI two-way
        Rs2Mod.API.getGridSearchBoxModeRegistry().add(ReiGridSearchBoxMode.create(queryParser, true, true, reiProxy)); // REI two-way autoselected
    }
}
