package com.refinedmods.refinedstorage2.platform.forge.integration.jei;

import net.minecraftforge.fml.ModList;

public class JeiIntegration {
    private static final String JEI_MOD_ID = "jei";

    private JeiIntegration() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(JEI_MOD_ID);
    }
}
