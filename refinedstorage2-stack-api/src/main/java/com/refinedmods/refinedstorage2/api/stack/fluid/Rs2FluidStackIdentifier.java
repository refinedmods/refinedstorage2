package com.refinedmods.refinedstorage2.api.stack.fluid;

import java.util.Objects;

public final class Rs2FluidStackIdentifier {
    private final Rs2Fluid fluid;
    private final Object tag;

    public Rs2FluidStackIdentifier(Rs2FluidStack stack) {
        this.fluid = stack.getFluid();
        this.tag = stack.getTag();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rs2FluidStackIdentifier that = (Rs2FluidStackIdentifier) o;
        return Objects.equals(fluid, that.fluid) &&
                Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluid, tag);
    }
}
