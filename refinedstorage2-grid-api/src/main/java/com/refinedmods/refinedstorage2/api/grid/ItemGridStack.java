package com.refinedmods.refinedstorage2.api.grid;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;

import java.util.Set;

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
