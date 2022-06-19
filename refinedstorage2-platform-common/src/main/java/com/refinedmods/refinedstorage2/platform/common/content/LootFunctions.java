package com.refinedmods.refinedstorage2.platform.common.content;

import java.util.function.Supplier;

import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public final class LootFunctions {
    public static final LootFunctions INSTANCE = new LootFunctions();

    private Supplier<LootItemFunctionType> storageBlock;

    private LootFunctions() {
    }

    public LootItemFunctionType getStorageBlock() {
        return storageBlock.get();
    }

    public void setStorageBlock(Supplier<LootItemFunctionType> storageBlockSupplier) {
        this.storageBlock = storageBlockSupplier;
    }
}
