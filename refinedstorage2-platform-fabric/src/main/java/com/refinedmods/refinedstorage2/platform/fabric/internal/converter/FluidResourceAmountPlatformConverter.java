package com.refinedmods.refinedstorage2.platform.fabric.internal.converter;

import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2Fluid;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.platform.fabric.api.converter.PlatformConverter;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.impl.transfer.fluid.FluidVariantImpl;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;

public class FluidResourceAmountPlatformConverter implements PlatformConverter<ResourceAmount<FluidVariant>, Rs2FluidStack> {
    private final PlatformConverter<Fluid, Rs2Fluid> fluidConverter;

    public FluidResourceAmountPlatformConverter(PlatformConverter<Fluid, Rs2Fluid> fluidConverter) {
        this.fluidConverter = fluidConverter;
    }

    @Override
    public ResourceAmount<FluidVariant> toPlatform(Rs2FluidStack value) {
        return new ResourceAmount<>(
                FluidVariantImpl.of(fluidConverter.toPlatform(value.getFluid()), value.getTag() != null ? (NbtCompound) value.getTag() : null),
                value.getAmount()
        );
    }

    @Override
    public Rs2FluidStack toDomain(ResourceAmount<FluidVariant> value) {
        if (value == null) {
            return Rs2FluidStack.EMPTY;
        }
        return new Rs2FluidStack(
                fluidConverter.toDomain(value.resource().getFluid()),
                value.amount(),
                value.resource().getNbt()
        );
    }
}
