package com.refinedmods.refinedstorage2.platform.forge.datagen.loot;

import java.util.List;
import java.util.Set;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import static net.minecraft.world.level.storage.loot.parameters.LootContextParams.EXPLOSION_RADIUS;

public class LootTableProvider extends net.minecraft.data.loot.LootTableProvider {
    public LootTableProvider(final PackOutput output) {
        super(output, Set.of(), List.of(new SubProviderEntry(
            BlockDropProvider::new,
            LootContextParamSet.builder().required(EXPLOSION_RADIUS).build())
        ));
    }

}
