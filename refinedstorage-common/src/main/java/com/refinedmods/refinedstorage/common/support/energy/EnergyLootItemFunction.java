package com.refinedmods.refinedstorage.common.support.energy;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class EnergyLootItemFunction extends AbstractEnergyLootItemFunction {
    public static final MapCodec<? extends LootItemFunction> FUNCTION_CODEC =
        MapCodec.unit(EnergyLootItemFunction::new);
    public static final Identifier NAME = createIdentifier("energy");

    @Override
    public MapCodec<? extends LootItemFunction> codec() {
        return FUNCTION_CODEC;
    }
}
