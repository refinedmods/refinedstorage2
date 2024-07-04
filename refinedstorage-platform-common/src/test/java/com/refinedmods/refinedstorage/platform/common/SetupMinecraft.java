package com.refinedmods.refinedstorage.platform.common;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SetupMinecraft implements BeforeAllCallback {
    @Override
    public void beforeAll(final ExtensionContext context) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
}

