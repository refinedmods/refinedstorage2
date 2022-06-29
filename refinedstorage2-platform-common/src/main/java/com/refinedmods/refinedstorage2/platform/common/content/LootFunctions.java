package com.refinedmods.refinedstorage2.platform.common.content;

import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public final class LootFunctions {
    public static final LootFunctions INSTANCE = new LootFunctions();

    @Nullable
    private Supplier<LootItemFunctionType> storageBlock;

    private LootFunctions() {
    }

    public LootItemFunctionType getStorageBlock() {
        return Objects.requireNonNull(storageBlock).get();
    }

    public void setStorageBlock(final Supplier<LootItemFunctionType> storageBlockSupplier) {
        this.storageBlock = storageBlockSupplier;
    }
}
