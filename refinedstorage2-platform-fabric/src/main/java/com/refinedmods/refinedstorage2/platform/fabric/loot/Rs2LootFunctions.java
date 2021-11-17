package com.refinedmods.refinedstorage2.platform.fabric.loot;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;

import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public class Rs2LootFunctions {
    private LootItemFunctionType controller;

    public void register() {
        controller = Registry.register(Registry.LOOT_FUNCTION_TYPE, Rs2Mod.createIdentifier("controller"), new LootItemFunctionType(new ControllerLootFunction.Serializer()));
    }

    public LootItemFunctionType getController() {
        return controller;
    }
}
