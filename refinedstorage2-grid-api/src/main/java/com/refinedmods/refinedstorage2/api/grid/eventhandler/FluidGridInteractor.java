package com.refinedmods.refinedstorage2.api.grid.eventhandler;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.storage.Source;

public interface FluidGridInteractor extends Source {
    Rs2FluidStack getCursorStack();

    Rs2FluidStack extractBucketFromCursor(Action action);

    Rs2FluidStack extractFromCursor(Action action, long amount);
}
