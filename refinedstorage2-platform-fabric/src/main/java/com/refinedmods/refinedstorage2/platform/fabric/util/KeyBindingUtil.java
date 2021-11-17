package com.refinedmods.refinedstorage2.platform.fabric.util;

import com.refinedmods.refinedstorage2.platform.fabric.mixin.KeyMappingAccessor;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class KeyBindingUtil {
    private KeyBindingUtil() {
    }

    public static boolean isKeyDown(KeyMapping keybinding) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), ((KeyMappingAccessor) keybinding).getKey().getValue());
    }
}
