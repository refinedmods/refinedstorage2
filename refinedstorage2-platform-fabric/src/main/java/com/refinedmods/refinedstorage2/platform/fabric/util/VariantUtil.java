package com.refinedmods.refinedstorage2.platform.fabric.util;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

public final class VariantUtil {
    private VariantUtil() {
    }

    public static ItemVariant toItemVariant(final ItemResource itemResource) {
        return ItemVariant.of(itemResource.item(), itemResource.tag());
    }

    public static ItemResource ofItemVariant(final ItemVariant itemVariant) {
        return new ItemResource(itemVariant.getItem(), itemVariant.getNbt());
    }

    public static FluidVariant toFluidVariant(final FluidResource fluidResource) {
        return FluidVariant.of(fluidResource.fluid(), fluidResource.tag());
    }

    public static FluidResource ofFluidVariant(final FluidVariant fluidVariant) {
        return new FluidResource(fluidVariant.getFluid(), fluidVariant.getNbt());
    }
}
