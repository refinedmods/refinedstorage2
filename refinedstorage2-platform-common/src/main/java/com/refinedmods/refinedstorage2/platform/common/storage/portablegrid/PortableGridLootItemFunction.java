package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.platform.common.content.LootFunctions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

class PortableGridLootItemFunction implements LootItemFunction {
    @Override
    public LootItemFunctionType getType() {
        return LootFunctions.INSTANCE.getPortableGrid();
    }

    @Override
    public ItemStack apply(final ItemStack itemStack, final LootContext lootContext) {
        // TODO: item representation of the portable grid
        return itemStack;
    }
}
