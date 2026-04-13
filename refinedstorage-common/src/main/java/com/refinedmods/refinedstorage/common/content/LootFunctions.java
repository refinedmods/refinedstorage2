package com.refinedmods.refinedstorage.common.content;

import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public final class LootFunctions {
    public static final LootFunctions INSTANCE = new LootFunctions();

    @Nullable
    private Supplier<MapCodec<? extends LootItemFunction>> storageBlock;
    @Nullable
    private Supplier<MapCodec<? extends LootItemFunction>> energy;
    @Nullable
    private Supplier<MapCodec<? extends LootItemFunction>> portableGrid;

    private LootFunctions() {
    }

    public MapCodec<? extends LootItemFunction> getStorageBlock() {
        return requireNonNull(storageBlock).get();
    }

    public void setStorageBlock(final Supplier<MapCodec<? extends LootItemFunction>> supplier) {
        this.storageBlock = supplier;
    }

    public MapCodec<? extends LootItemFunction> getEnergy() {
        return requireNonNull(energy).get();
    }

    public void setEnergy(final Supplier<MapCodec<? extends LootItemFunction>> supplier) {
        this.energy = supplier;
    }

    public MapCodec<? extends LootItemFunction> getPortableGrid() {
        return requireNonNull(portableGrid).get();
    }

    public void setPortableGrid(final Supplier<MapCodec<? extends LootItemFunction>> supplier) {
        this.portableGrid = supplier;
    }
}
