package com.refinedmods.refinedstorage2.core.grid;

import net.minecraft.item.ItemStack;

public interface GridInteractor {
    ItemStack getCursorStack();

    void setCursorStack(ItemStack stack);

    ItemStack insertIntoInventory(ItemStack stack);
}
