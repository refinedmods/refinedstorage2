package com.refinedmods.refinedstorage2.api.grid.eventhandler;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;

public interface ItemGridEventHandler {
    void onInsertFromCursor(GridInsertMode mode);

    Rs2ItemStack onInsertFromTransfer(Rs2ItemStack slotStack);

    void onExtract(Rs2ItemStack stack, GridExtractMode mode);

    void onActiveChanged(boolean active);

    void onScroll(Rs2ItemStack template, int slot, GridScrollMode mode);
}
