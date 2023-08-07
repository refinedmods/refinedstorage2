package com.refinedmods.refinedstorage2.platform.common.internal.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceInstance;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;

public class ItemResourceFactory implements ResourceFactory<ItemResource> {
    @Override
    public Optional<ResourceInstance<ItemResource>> create(final ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ResourceInstance<>(
            new ResourceAmount<>(ItemResource.ofItemStack(stack), stack.getCount()),
            StorageChannelTypes.ITEM
        ));
    }
}
