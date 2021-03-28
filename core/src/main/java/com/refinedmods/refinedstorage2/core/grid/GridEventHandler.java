package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.storage.StorageTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public interface GridEventHandler {
    void onInsertFromCursor(GridInsertMode mode);

    void onInsertFromTransfer(Slot slot);

    void onExtract(ItemStack stack, GridExtractMode mode);

    void onItemUpdate(ItemStack template, int amount, StorageTracker.Entry trackerEntry);

    void onActiveChanged(boolean active);

    void onScroll(ItemStack template, int slot, GridScrollMode mode);
}
