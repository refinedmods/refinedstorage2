package com.refinedmods.refinedstorage2.platform.forge.util;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;

import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public final class VariantUtil {
    private VariantUtil() {
    }

    public static FluidStack toFluidStack(FluidResource fluidResource) {
        return new FluidStack(fluidResource.getFluid(), FluidAttributes.BUCKET_VOLUME, fluidResource.getTag());
    }
}
