package com.refinedmods.refinedstorage2.platform.fabric.integration.jei;

import javax.annotation.Nullable;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class JeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = createIdentifier("plugin");

    @Nullable
    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void onRuntimeAvailable(final IJeiRuntime runtime) {
        JeiPlugin.runtime = runtime;
    }

    @Nullable
    public static IJeiRuntime getRuntime() {
        return runtime;
    }
}
