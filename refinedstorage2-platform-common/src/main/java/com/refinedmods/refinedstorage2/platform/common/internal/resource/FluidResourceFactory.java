package com.refinedmods.refinedstorage2.platform.common.internal.resource;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;

public class FluidResourceFactory implements ResourceFactory<FluidResource> {
    @Override
    public Optional<ResourceAmountTemplate<FluidResource>> create(final ItemStack stack) {
        return Platform.INSTANCE.convertToFluid(stack).map(resourceAmount -> new ResourceAmountTemplate<>(
            resourceAmount.getResource(),
            Platform.INSTANCE.getBucketAmount(),
            StorageChannelTypes.FLUID
        ));
    }
}
