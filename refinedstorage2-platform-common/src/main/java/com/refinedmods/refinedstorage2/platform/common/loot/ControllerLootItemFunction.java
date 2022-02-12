package com.refinedmods.refinedstorage2.platform.common.loot;

import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.LootFunctions;
import com.refinedmods.refinedstorage2.platform.common.item.block.ControllerBlockItem;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ControllerLootItemFunction implements LootItemFunction {
    @Override
    public LootItemFunctionType getType() {
        return LootFunctions.INSTANCE.getController();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        BlockEntity controller = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);

        if (controller instanceof ControllerBlockEntity controllerBlockEntity) {
            long stored = controllerBlockEntity.getActualStored();
            long capacity = controllerBlockEntity.getActualCapacity();

            ControllerBlockItem.setEnergy(stack, stored, capacity);
        }

        return stack;
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ControllerLootItemFunction> {
        @Override
        public void serialize(JsonObject json, ControllerLootItemFunction value, JsonSerializationContext serializationContext) {
            // no serialization necessary
        }

        @Override
        public ControllerLootItemFunction deserialize(JsonObject json, JsonDeserializationContext context) {
            return new ControllerLootItemFunction();
        }
    }
}
