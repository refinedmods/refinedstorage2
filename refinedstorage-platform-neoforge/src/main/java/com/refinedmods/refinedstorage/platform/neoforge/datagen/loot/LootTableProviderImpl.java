package com.refinedmods.refinedstorage.platform.neoforge.datagen.loot;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class LootTableProviderImpl extends LootTableProvider {
    public LootTableProviderImpl(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider) {
        super(output, Set.of(), List.of(new SubProviderEntry(
            BlockDropProvider::new,
            LootContextParamSets.BLOCK
        )), provider);
    }
}
