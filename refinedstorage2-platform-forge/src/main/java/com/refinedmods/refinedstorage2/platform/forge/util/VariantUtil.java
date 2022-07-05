package com.refinedmods.refinedstorage2.platform.forge.util;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public final class VariantUtil {
    private VariantUtil() {
    }

    public static FluidResource ofFluidStack(final FluidStack fluidStack) {
        return new FluidResource(fluidStack.getFluid(), fluidStack.getTag());
    }

    public static FluidStack toFluidStack(final FluidResource fluidResource, final long amount) {
        return new FluidStack(fluidResource.fluid(), (int) amount, fluidResource.tag());
    }

    public static ItemResource ofItemStack(final ItemStack itemStack) {
        return new ItemResource(itemStack.getItem(), itemStack.getTag());
    }

    public static ItemStack toItemStack(final ItemResource itemResource, final long amount) {
        return new ItemStack(itemResource.item(), (int) amount, itemResource.tag());
    }
}
