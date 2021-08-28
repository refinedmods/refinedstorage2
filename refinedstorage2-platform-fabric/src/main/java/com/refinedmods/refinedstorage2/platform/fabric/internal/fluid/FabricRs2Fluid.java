package com.refinedmods.refinedstorage2.platform.fabric.internal.fluid;

import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2Fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.util.registry.Registry;

public class FabricRs2Fluid implements Rs2Fluid {
    private final FluidVariant fluidVariant;
    private final String identifier;

    public FabricRs2Fluid(FluidVariant fluidVariant) {
        this.fluidVariant = fluidVariant;
        this.identifier = Registry.FLUID.getId(fluidVariant.getFluid()).toString();
    }

    public FluidVariant getFluidVariant() {
        return fluidVariant;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
}
