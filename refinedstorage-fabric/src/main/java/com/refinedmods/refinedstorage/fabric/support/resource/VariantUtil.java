package com.refinedmods.refinedstorage.fabric.support.resource;

import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

public final class VariantUtil {
    private VariantUtil() {
    }

    public static ItemVariant toItemVariant(final ItemResource itemResource) {
        return ItemVariant.of(itemResource.item(), itemResource.components());
    }

    public static ItemResource ofItemVariant(final ItemVariant itemVariant) {
        return new ItemResource(itemVariant.getItem(), itemVariant.getComponentsPatch());
    }

    public static FluidVariant toFluidVariant(final FluidResource fluidResource) {
        return FluidVariant.of(fluidResource.fluid(), fluidResource.components());
    }

    public static FluidResource ofFluidVariant(final FluidVariant fluidVariant) {
        return new FluidResource(fluidVariant.getFluid(), fluidVariant.getComponentsPatch());
    }
}
