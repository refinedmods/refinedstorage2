package com.refinedmods.refinedstorage2.api.grid.eventhandler;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;

public interface ItemGridEventHandler {
    void onInsertFromCursor(GridInsertMode mode);

    Rs2ItemStack onInsertFromTransfer(Rs2ItemStack slotStack);

    void onExtract(Rs2ItemStack stack, GridExtractMode mode);

    void onItemUpdate(Rs2ItemStack template, long amount, StorageTracker.Entry trackerEntry);

    void onActiveChanged(boolean active);

    boolean isActive();

    void onScroll(Rs2ItemStack template, int slot, GridScrollMode mode);
}
