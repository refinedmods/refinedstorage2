package com.refinedmods.refinedstorage2.platform.common.content;

import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public final class LootFunctions {
    public static final LootFunctions INSTANCE = new LootFunctions();

    private LootItemFunctionType storageBlock;

    private LootFunctions() {
    }

    public LootItemFunctionType getStorageBlock() {
        return storageBlock;
    }

    public void setStorageBlock(LootItemFunctionType storageBlock) {
        this.storageBlock = storageBlock;
    }
}
