package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.stack;

import com.refinedmods.refinedstorage2.api.grid.view.stack.ItemGridStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.platform.fabric.api.util.ItemStacks;

import java.util.Set;

import net.minecraft.item.ItemStack;

public class FabricItemGridStack extends ItemGridStack {
    private final ItemStack mcStack;

    public FabricItemGridStack(Rs2ItemStack stack, String name, String modId, String modName, Set<String> tags) {
        super(stack, name, modId, modName, tags);
        this.mcStack = ItemStacks.toItemStack(stack);
    }

    public ItemStack getMcStack() {
        return mcStack;
    }
}
