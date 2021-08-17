package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.storage.Source;
import com.refinedmods.refinedstorage2.core.util.Action;

public interface GridInteractor extends Source {
    Rs2ItemStack getCursorStack();

    void setCursorStack(Rs2ItemStack stack);

    Rs2ItemStack insertIntoInventory(Rs2ItemStack stack, int preferredSlot, Action action);

    Rs2ItemStack extractFromInventory(Rs2ItemStack template, int slot, long count, Action action);
}
