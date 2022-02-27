package com.refinedmods.refinedstorage2.platform.fabric.integration.rei;

import net.fabricmc.loader.api.FabricLoader;

public class ReiIntegration {
    private static final String REI_MOD_ID = "roughlyenoughitems";

    private ReiIntegration() {
    }

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(REI_MOD_ID);
    }
}
