package com.refinedmods.refinedstorage2.core;

import net.minecraft.Bootstrap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class BootstrapInitializeCallback implements BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) {
        Bootstrap.initialize();
    }
}
