package com.refinedmods.refinedstorage.neoforge.support.resource;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import org.jspecify.annotations.Nullable;

public final class VariantUtil {
    private VariantUtil() {
    }

    public static FluidResource ofPlatform(final net.neoforged.neoforge.transfer.fluid.FluidResource fluidResource) {
        return new FluidResource(fluidResource.getFluid(), fluidResource.getComponentsPatch());
    }

    public static ItemResource ofPlatform(final net.neoforged.neoforge.transfer.item.ItemResource itemResource) {
        return new ItemResource(itemResource.getItem(), itemResource.getComponentsPatch());
    }

    public static net.neoforged.neoforge.transfer.fluid.@Nullable FluidResource optionalFluidToPlatform(
        final ResourceKey resource
    ) {
        if (!(resource instanceof FluidResource fluidResource)) {
            return null;
        }
        return net.neoforged.neoforge.transfer.fluid.FluidResource.of(
            fluidResource.fluid(),
            fluidResource.components()
        );
    }

    public static net.neoforged.neoforge.transfer.item.@Nullable ItemResource optionalItemToPlatform(
        final ResourceKey resource
    ) {
        if (!(resource instanceof ItemResource itemResource)) {
            return null;
        }
        return net.neoforged.neoforge.transfer.item.ItemResource.of(
            itemResource.item(),
            itemResource.components()
        );
    }

    public static net.neoforged.neoforge.transfer.fluid.FluidResource toPlatform(final FluidResource fluidResource) {
        return net.neoforged.neoforge.transfer.fluid.FluidResource.of(
            fluidResource.fluid(),
            fluidResource.components()
        );
    }

    public static net.neoforged.neoforge.transfer.item.ItemResource toPlatform(final ItemResource itemResource) {
        return net.neoforged.neoforge.transfer.item.ItemResource.of(
            itemResource.item(),
            itemResource.components()
        );
    }
}
