package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.storage.StorageTracker;

public interface GridEventHandler {
    void onInsertFromCursor(GridInsertMode mode);

    Rs2ItemStack onInsertFromTransfer(Rs2ItemStack slotStack);

    void onExtract(Rs2ItemStack stack, GridExtractMode mode);

    void onItemUpdate(Rs2ItemStack template, long amount, StorageTracker.Entry trackerEntry);

    void onActiveChanged(boolean active);

    boolean isActive();

    void onScroll(Rs2ItemStack template, int slot, GridScrollMode mode);
}
