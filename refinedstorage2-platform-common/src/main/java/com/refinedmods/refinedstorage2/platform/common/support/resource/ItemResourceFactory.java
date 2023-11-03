package com.refinedmods.refinedstorage2.platform.common.support.resource;

import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;

public class ItemResourceFactory implements ResourceFactory<ItemResource> {
    @Override
    public Optional<ResourceAmountTemplate<ItemResource>> create(final ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ResourceAmountTemplate<>(
            ItemResource.ofItemStack(stack),
            stack.getCount(),
            StorageChannelTypes.ITEM
        ));
    }

    @Override
    public boolean isValid(final Object resource) {
        return resource instanceof ItemResource;
    }
}
