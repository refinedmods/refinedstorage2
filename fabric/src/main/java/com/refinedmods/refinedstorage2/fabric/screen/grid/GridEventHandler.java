package com.refinedmods.refinedstorage2.fabric.screen.grid;

import net.minecraft.item.ItemStack;

public interface GridEventHandler {
    void onInsertFromCursor(boolean single);

    void onItemUpdate(ItemStack template, int amount);
}
