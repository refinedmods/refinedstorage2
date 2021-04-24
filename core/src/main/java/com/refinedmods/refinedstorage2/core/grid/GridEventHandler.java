package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.storage.StorageTracker;
import net.minecraft.item.ItemStack;

public interface GridEventHandler {
    void onInsertFromCursor(GridInsertMode mode);

    ItemStack onInsertFromTransfer(ItemStack slotStack);

    void onExtract(ItemStack stack, GridExtractMode mode);

    void onItemUpdate(ItemStack template, int amount, StorageTracker.Entry trackerEntry);

    void onActiveChanged(boolean active);

    void onScroll(ItemStack template, int slot, GridScrollMode mode);
}
