package com.refinedmods.refinedstorage2.platform.fabric.internal.converter;

import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2Fluid;
import com.refinedmods.refinedstorage2.platform.fabric.api.converter.PlatformConverter;
import com.refinedmods.refinedstorage2.platform.fabric.internal.fluid.FabricRs2Fluid;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.fluid.Fluid;

// TODO: add tests for all impls
public class FluidPlatformConverter implements PlatformConverter<Fluid, Rs2Fluid> {
    private final Map<Fluid, Rs2Fluid> fluidCache = new HashMap<>();

    @Override
    public Fluid toPlatform(Rs2Fluid value) {
        return ((FabricRs2Fluid) value).getFluid();
    }

    @Override
    public Rs2Fluid toDomain(Fluid value) {
        return fluidCache.computeIfAbsent(value, FabricRs2Fluid::new);
    }
}
