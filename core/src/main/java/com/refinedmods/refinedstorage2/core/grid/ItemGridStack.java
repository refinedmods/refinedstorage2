package com.refinedmods.refinedstorage2.core.grid;

import java.util.Set;

import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;

public class ItemGridStack extends GridStack<Rs2ItemStack> {
    public ItemGridStack(Rs2ItemStack stack, String name, String modId, String modName, Set<String> tags) {
        super(stack, name, modId, modName, tags);
    }

    @Override
    public int getId() {
        return getStack().getItem().getId();
    }

    @Override
    public long getAmount() {
        return getStack().getAmount();
    }
}
