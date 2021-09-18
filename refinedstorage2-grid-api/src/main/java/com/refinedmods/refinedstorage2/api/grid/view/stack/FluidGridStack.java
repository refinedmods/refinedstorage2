package com.refinedmods.refinedstorage2.api.grid.view.stack;

import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;

import java.util.Set;

public class FluidGridStack extends GridStack<Rs2FluidStack> {
    public FluidGridStack(ResourceAmount<Rs2FluidStack> resourceAmount, String name, String modId, String modName, Set<String> tags) {
        super(resourceAmount, name, modId, modName, tags);
    }

    @Override
    public int getId() {
        return getResourceAmount().getResource().getFluid().getId();
    }
}
