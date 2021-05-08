package com.refinedmods.refinedstorage2.fabric.loot;

import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.fabric.item.block.ControllerBlockItem;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.JsonSerializer;

public class ControllerLootFunction implements LootFunction {
    @Override
    public LootFunctionType getType() {
        return Rs2Mod.LOOT_FUNCTIONS.getController();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        BlockEntity controller = lootContext.get(LootContextParameters.BLOCK_ENTITY);

        if (controller instanceof ControllerBlockEntity) {
            long stored = ((ControllerBlockEntity) controller).getActualStored();
            long capacity = ((ControllerBlockEntity) controller).getActualCapacity();
            int damage = ControllerBlockItem.calculateDamage(stored, capacity);

            stack.setDamage(damage);
            ControllerBlockItem.setEnergy(stack, stored, capacity);
        }

        return stack;
    }

    public static class Serializer implements JsonSerializer<ControllerLootFunction> {
        @Override
        public void toJson(JsonObject json, ControllerLootFunction object, JsonSerializationContext context) {

        }

        @Override
        public ControllerLootFunction fromJson(JsonObject json, JsonDeserializationContext context) {
            return new ControllerLootFunction();
        }
    }
}
