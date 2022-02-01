package com.refinedmods.refinedstorage2.platform.forge.integration.jei;

import com.refinedmods.refinedstorage2.api.grid.search.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParser;

import net.minecraftforge.fml.ModList;

public class JeiIntegration {
    private static final String JEI_MOD_ID = "jei";

    private JeiIntegration() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(JEI_MOD_ID);
    }

    public static void registerGridSearchBoxModes(GridQueryParser queryParser) {
        JeiProxy jeiProxy = new JeiProxy();

        GridSearchBoxModeRegistry.INSTANCE.add(JeiGridSearchBoxMode.create(queryParser, false, false, jeiProxy)); // REI
        GridSearchBoxModeRegistry.INSTANCE.add(JeiGridSearchBoxMode.create(queryParser, true, false, jeiProxy)); // REI autoselected

        GridSearchBoxModeRegistry.INSTANCE.add(JeiGridSearchBoxMode.create(queryParser, false, true, jeiProxy)); // REI two-way
        GridSearchBoxModeRegistry.INSTANCE.add(JeiGridSearchBoxMode.create(queryParser, true, true, jeiProxy)); // REI two-way autoselected
    }
}
