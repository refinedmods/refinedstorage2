package com.refinedmods.refinedstorage2.api.grid.eventhandler;

import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;

public interface FluidGridEventHandler {
    void onInsertFromCursor();

    long onInsertFromTransfer(Rs2FluidStack stack);

    void onActiveChanged(boolean active);
}
