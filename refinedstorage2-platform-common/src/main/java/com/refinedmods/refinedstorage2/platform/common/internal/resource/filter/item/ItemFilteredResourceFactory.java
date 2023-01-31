package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResourceFactory;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;

public class ItemFilteredResourceFactory implements FilteredResourceFactory {
    @Override
    public Optional<FilteredResource<?>> create(final ItemStack stack, final boolean tryAlternatives) {
        if (!stack.isEmpty()) {
            return Optional.of(new ItemFilteredResource(ItemResource.ofItemStack(stack), stack.getCount()));
        }
        return Optional.empty();
    }
}
