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

        GridSearchBoxModeRegistry.INSTANCE.add(JeiGridSearchBoxMode.create(queryParser, false, false, jeiProxy)); // JEI
        GridSearchBoxModeRegistry.INSTANCE.add(JeiGridSearchBoxMode.create(queryParser, true, false, jeiProxy)); // JEI autoselected

        GridSearchBoxModeRegistry.INSTANCE.add(JeiGridSearchBoxMode.create(queryParser, false, true, jeiProxy)); // JEI two-way
        GridSearchBoxModeRegistry.INSTANCE.add(JeiGridSearchBoxMode.create(queryParser, true, true, jeiProxy)); // JEI two-way autoselected
    }
}
