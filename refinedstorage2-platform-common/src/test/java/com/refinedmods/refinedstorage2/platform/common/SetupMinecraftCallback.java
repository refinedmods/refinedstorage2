package com.refinedmods.refinedstorage2.platform.common;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SetupMinecraftCallback implements BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
}
