package com.refinedmods.refinedstorage2.platform.common.support.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceFactory;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;

public class ItemResourceFactory implements ResourceFactory {
    @Override
    public Optional<ResourceAmount> create(final ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ResourceAmount(ItemResource.ofItemStack(stack), stack.getCount()));
    }

    @Override
    public boolean isValid(final ResourceKey resource) {
        return resource instanceof ItemResource;
    }
}
