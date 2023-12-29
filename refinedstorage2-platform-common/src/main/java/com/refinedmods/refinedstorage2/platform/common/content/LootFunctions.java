package com.refinedmods.refinedstorage2.platform.common.content;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import static java.util.Objects.requireNonNull;

public final class LootFunctions {
    public static final LootFunctions INSTANCE = new LootFunctions();

    @Nullable
    private Supplier<LootItemFunctionType> storageBlock;
    @Nullable
    private Supplier<LootItemFunctionType> energy;
    @Nullable
    private Supplier<LootItemFunctionType> portableGrid;

    private LootFunctions() {
    }

    public LootItemFunctionType getStorageBlock() {
        return requireNonNull(storageBlock).get();
    }

    public void setStorageBlock(final Supplier<LootItemFunctionType> supplier) {
        this.storageBlock = supplier;
    }

    public LootItemFunctionType getEnergy() {
        return requireNonNull(energy).get();
    }

    public void setEnergy(final Supplier<LootItemFunctionType> supplier) {
        this.energy = supplier;
    }

    public LootItemFunctionType getPortableGrid() {
        return requireNonNull(portableGrid).get();
    }

    public void setPortableGrid(final Supplier<LootItemFunctionType> portableGrid) {
        this.portableGrid = portableGrid;
    }
}
