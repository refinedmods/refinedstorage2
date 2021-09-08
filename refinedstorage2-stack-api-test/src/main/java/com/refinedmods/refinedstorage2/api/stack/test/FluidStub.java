package com.refinedmods.refinedstorage2.api.stack.test;

import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2Fluid;

public record FluidStub(int id, String identifier) implements Rs2Fluid {
    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return identifier;
    }
}
