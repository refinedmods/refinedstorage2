package com.refinedmods.refinedstorage2.api.grid.eventhandler;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;

public class FakeFluidGridInteractor implements FluidGridInteractor {
    private static final String NAME = "Fake interactor";

    private Rs2FluidStack cursorStack = Rs2FluidStack.EMPTY;
    private final long bucketAmount;

    public FakeFluidGridInteractor(long bucketAmount) {
        this.bucketAmount = bucketAmount;
    }

    public void setCursorStack(Rs2FluidStack cursorStack) {
        this.cursorStack = cursorStack;
    }

    @Override
    public Rs2FluidStack getCursorStack() {
        return cursorStack;
    }

    @Override
    public Rs2FluidStack extractBucketFromCursor(Action action) {
        return extractFromCursor(action, bucketAmount);
    }

    @Override
    public Rs2FluidStack extractFromCursor(Action action, long amount) {
        if (cursorStack.isEmpty()) {
            return Rs2FluidStack.EMPTY;
        }
        long toExtract = Math.min(cursorStack.getAmount(), amount);
        Rs2FluidStack extracted = cursorStack.copy();
        extracted.setAmount(toExtract);
        if (action == Action.EXECUTE) {
            cursorStack.decrement(toExtract);
        }
        return extracted;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
