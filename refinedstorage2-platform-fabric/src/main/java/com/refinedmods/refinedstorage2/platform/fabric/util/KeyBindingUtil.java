package com.refinedmods.refinedstorage2.platform.fabric.util;

import com.refinedmods.refinedstorage2.platform.fabric.mixin.KeyBindingAccessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public final class KeyBindingUtil {
    private KeyBindingUtil() {
    }

    public static boolean isKeyDown(KeyBinding keybinding) {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), ((KeyBindingAccessor) keybinding).getBoundKey().getCode());
    }
}
