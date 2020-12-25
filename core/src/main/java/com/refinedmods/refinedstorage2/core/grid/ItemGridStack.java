package com.refinedmods.refinedstorage2.core.grid;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Set;

public class ItemGridStack extends GridStack<ItemStack> {
    public ItemGridStack(ItemStack stack, String name, String modId, String modName, Set<String> tags) {
        super(stack, name, modId, modName, tags);
    }

    @Override
    public int getId() {
        return Item.getRawId(getStack().getItem());
    }

    @Override
    public int getCount() {
        return getStack().getCount();
    }
}
