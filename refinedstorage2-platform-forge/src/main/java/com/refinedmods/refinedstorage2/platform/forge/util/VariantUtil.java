package com.refinedmods.refinedstorage2.platform.forge.util;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public final class VariantUtil {
    private VariantUtil() {
    }

    public static FluidResource ofFluidStack(FluidStack fluidStack) {
        return new FluidResource(fluidStack.getFluid(), fluidStack.getTag());
    }

    public static FluidStack toFluidStack(FluidResource fluidResource, long amount) {
        return new FluidStack(fluidResource.fluid(), (int) amount, fluidResource.tag());
    }

    public static ItemResource ofItemStack(ItemStack itemStack) {
        return new ItemResource(itemStack.getItem(), itemStack.getTag());
    }

    public static ItemStack toItemStack(ItemResource itemResource, long amount) {
        return new ItemStack(itemResource.item(), (int) amount, itemResource.tag());
    }
}
