package com.refinedmods.refinedstorage2.fabric;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RefinedStorage2Mod implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger(RefinedStorage2Mod.class);

    @Override
    public void onInitialize() {
        LOGGER.info("Refined Storage 2 has loaded.");
    }
}
