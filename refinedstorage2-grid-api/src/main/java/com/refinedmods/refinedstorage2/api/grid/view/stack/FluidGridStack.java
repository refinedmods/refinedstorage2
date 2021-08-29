package com.refinedmods.refinedstorage2.api.grid.view.stack;

import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;

import java.util.Set;

public class FluidGridStack extends GridStack<Rs2FluidStack> {
    public FluidGridStack(Rs2FluidStack stack, String name, String modId, String modName, Set<String> tags) {
        super(stack, name, modId, modName, tags);
    }

    @Override
    public int getId() {
        return getStack().getFluid().getId();
    }

    @Override
    public long getAmount() {
        return getStack().getAmount();
    }
}
