package com.refinedmods.refinedstorage2.platform.common.support.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;

public class FluidResourceFactory implements ResourceFactory {
    @Override
    public Optional<ResourceAmount> create(final ItemStack stack) {
        return Platform.INSTANCE.getContainedFluid(stack).map(result -> new ResourceAmount(
            result.fluid(),
            Platform.INSTANCE.getBucketAmount()
        ));
    }

    @Override
    public boolean isValid(final ResourceKey resource) {
        return resource instanceof FluidResource;
    }
}
