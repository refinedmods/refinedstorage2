package com.refinedmods.refinedstorage.platform.common.content;

import com.refinedmods.refinedstorage.platform.common.storage.portablegrid.PortableGridLootItemFunction;
import com.refinedmods.refinedstorage.platform.common.storage.storageblock.StorageBlockLootItemFunction;
import com.refinedmods.refinedstorage.platform.common.support.energy.EnergyLootItemFunction;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import static java.util.Objects.requireNonNull;

public final class LootFunctions {
    public static final LootFunctions INSTANCE = new LootFunctions();

    @Nullable
    private Supplier<LootItemFunctionType<StorageBlockLootItemFunction>> storageBlock;
    @Nullable
    private Supplier<LootItemFunctionType<EnergyLootItemFunction>> energy;
    @Nullable
    private Supplier<LootItemFunctionType<PortableGridLootItemFunction>> portableGrid;

    private LootFunctions() {
    }

    public LootItemFunctionType<StorageBlockLootItemFunction> getStorageBlock() {
        return requireNonNull(storageBlock).get();
    }

    public void setStorageBlock(final Supplier<LootItemFunctionType<StorageBlockLootItemFunction>> supplier) {
        this.storageBlock = supplier;
    }

    public LootItemFunctionType<EnergyLootItemFunction> getEnergy() {
        return requireNonNull(energy).get();
    }

    public void setEnergy(final Supplier<LootItemFunctionType<EnergyLootItemFunction>> supplier) {
        this.energy = supplier;
    }

    public LootItemFunctionType<PortableGridLootItemFunction> getPortableGrid() {
        return requireNonNull(portableGrid).get();
    }

    public void setPortableGrid(final Supplier<LootItemFunctionType<PortableGridLootItemFunction>> supplier) {
        this.portableGrid = supplier;
    }
}
