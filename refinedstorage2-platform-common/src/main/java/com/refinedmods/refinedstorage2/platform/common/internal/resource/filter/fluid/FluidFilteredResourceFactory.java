package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.fluid;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResourceFactory;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;

public class FluidFilteredResourceFactory implements FilteredResourceFactory {
    @Override
    public Optional<FilteredResource<?>> create(final ItemStack stack, final boolean tryAlternatives) {
        return Platform.INSTANCE.convertToFluid(stack).map(
            fluidResource -> new FluidFilteredResource(fluidResource, Platform.INSTANCE.getBucketAmount())
        );
    }
}
