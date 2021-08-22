package com.refinedmods.refinedstorage2.api.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.storage.Source;

public interface GridInteractor extends Source {
    Rs2ItemStack getCursorStack();

    void setCursorStack(Rs2ItemStack stack);

    Rs2ItemStack insertIntoInventory(Rs2ItemStack stack, int preferredSlot, Action action);

    Rs2ItemStack extractFromInventory(Rs2ItemStack template, int slot, long count, Action action);
}
