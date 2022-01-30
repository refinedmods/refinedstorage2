package com.refinedmods.refinedstorage2.platform.fabric.integration.rei;

import com.refinedmods.refinedstorage2.api.grid.search.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParser;

import net.fabricmc.loader.api.FabricLoader;

public class ReiIntegration {
    private static final String REI_MOD_ID = "roughlyenoughitems-api";

    private ReiIntegration() {
    }

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(REI_MOD_ID);
    }

    public static void registerGridSearchBoxModes(GridQueryParser queryParser) {
        ReiProxy reiProxy = new ReiProxy();

        GridSearchBoxModeRegistry.INSTANCE.add(ReiGridSearchBoxMode.create(queryParser, false, false, reiProxy)); // REI
        GridSearchBoxModeRegistry.INSTANCE.add(ReiGridSearchBoxMode.create(queryParser, true, false, reiProxy)); // REI autoselected

        GridSearchBoxModeRegistry.INSTANCE.add(ReiGridSearchBoxMode.create(queryParser, false, true, reiProxy)); // REI two-way
        GridSearchBoxModeRegistry.INSTANCE.add(ReiGridSearchBoxMode.create(queryParser, true, true, reiProxy)); // REI two-way autoselected
    }
}