package com.refinedmods.refinedstorage2.platform.common.content;

import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public final class LootFunctions {
    public static final LootFunctions INSTANCE = new LootFunctions();

    private LootItemFunctionType controller;

    private LootFunctions() {
    }

    public LootItemFunctionType getController() {
        return controller;
    }

    public void setController(LootItemFunctionType controller) {
        this.controller = controller;
    }
}
