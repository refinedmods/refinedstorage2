package com.refinedmods.refinedstorage2.platform.fabric.internal.fluid;

import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2Fluid;

import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FabricRs2Fluid implements Rs2Fluid {
    private final Fluid fluid;
    private final Identifier identifier;
    private final int rawId;

    public FabricRs2Fluid(Fluid fluid) {
        this.fluid = fluid;
        this.rawId = Registry.FLUID.getRawId(fluid);
        this.identifier = Registry.FLUID.getId(fluid);
    }

    public Fluid getFluid() {
        return fluid;
    }

    @Override
    public String getIdentifier() {
        return identifier.toString();
    }

    @Override
    public int getId() {
        return rawId;
    }
}
