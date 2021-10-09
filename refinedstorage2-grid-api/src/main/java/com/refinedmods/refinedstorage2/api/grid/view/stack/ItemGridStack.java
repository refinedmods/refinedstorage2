package com.refinedmods.refinedstorage2.api.grid.view.stack;

import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;

import java.util.Set;

public class ItemGridStack extends GridStack<Rs2ItemStack> {
    public ItemGridStack(ResourceAmount<Rs2ItemStack> stack, String name, String modId, String modName, Set<String> tags) {
        super(stack, name, modId, modName, tags);
    }

    @Override
    public int getId() {
        return getResourceAmount().getResource().getItem().getId();
    }
}
