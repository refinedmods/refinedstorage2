package com.refinedmods.refinedstorage2.platform.fabric.internal.fluid;

import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2Fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FabricRs2Fluid implements Rs2Fluid {
    private final FluidVariant fluidVariant;
    private final Identifier identifier;
    private final int rawId;

    public FabricRs2Fluid(FluidVariant fluidVariant) {
        this.fluidVariant = fluidVariant;
        this.rawId = Registry.FLUID.getRawId(fluidVariant.getFluid());
        this.identifier = Registry.FLUID.getId(fluidVariant.getFluid());
    }

    public FluidVariant getFluidVariant() {
        return fluidVariant;
    }

    @Override
    public String getIdentifier() {
        return identifier.toString();
    }

    @Override
    public int getId() {
        return rawId;
    }

    @Override
    public String getName() {
        return fluidVariant.getFluid().toString();
    }
}
