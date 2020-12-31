package com.refinedmods.refinedstorage2.core.grid;

import net.minecraft.item.ItemStack;

public interface GridEventHandler {
    void onInsertFromCursor(GridInsertMode mode);

    void onExtract(ItemStack stack, GridExtractMode mode);

    void onItemUpdate(ItemStack template, int amount);

    void onScroll(ItemStack template, int slot, GridScrollMode mode);
}
