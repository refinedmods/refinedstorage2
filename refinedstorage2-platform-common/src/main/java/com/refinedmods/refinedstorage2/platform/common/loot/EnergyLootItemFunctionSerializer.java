package com.refinedmods.refinedstorage2.platform.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class EnergyLootItemFunctionSerializer implements Serializer<LootItemFunction> {
    @Override
    public void serialize(final JsonObject jsonObject,
                          final LootItemFunction lootItemFunction,
                          final JsonSerializationContext jsonSerializationContext) {
        // no op
    }

    @Override
    public LootItemFunction deserialize(final JsonObject jsonObject,
                                        final JsonDeserializationContext jsonDeserializationContext) {
        return new EnergyLootItemFunction();
    }
}
