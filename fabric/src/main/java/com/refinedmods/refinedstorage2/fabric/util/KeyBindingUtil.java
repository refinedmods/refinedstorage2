package com.refinedmods.refinedstorage2.fabric.util;

import com.refinedmods.refinedstorage2.fabric.mixin.KeyBindingAccessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class KeyBindingUtil {
    public static boolean isKeyDown(KeyBinding keybinding) {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), ((KeyBindingAccessor) keybinding).getBoundKey().getCode());
    }
}
