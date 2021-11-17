package com.refinedmods.refinedstorage2.platform.fabric.loot;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.item.block.ControllerBlockItem;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ControllerLootFunction implements LootItemFunction {
    @Override
    public LootItemFunctionType getType() {
        return Rs2Mod.LOOT_FUNCTIONS.getController();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        BlockEntity controller = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);

        if (controller instanceof ControllerBlockEntity controllerBlockEntity) {
            long stored = controllerBlockEntity.getActualStored();
            long capacity = controllerBlockEntity.getActualCapacity();
            int damage = ControllerBlockItem.calculateDamage(stored, capacity);

            stack.setDamageValue(damage);
            ControllerBlockItem.setEnergy(stack, stored, capacity);
        }

        return stack;
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ControllerLootFunction> {
        @Override
        public void serialize(JsonObject json, ControllerLootFunction value, JsonSerializationContext serializationContext) {
            // no serialization necessary
        }

        @Override
        public ControllerLootFunction deserialize(JsonObject json, JsonDeserializationContext context) {
            return new ControllerLootFunction();
        }
    }
}
