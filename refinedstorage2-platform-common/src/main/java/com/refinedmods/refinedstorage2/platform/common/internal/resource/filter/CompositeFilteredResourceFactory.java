package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResourceFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.world.item.ItemStack;

public class CompositeFilteredResourceFactory implements FilteredResourceFactory {
    private final FilteredResourceFactory defaultFactory;
    private final List<FilteredResourceFactory> alternatives = new ArrayList<>();

    public CompositeFilteredResourceFactory(final FilteredResourceFactory defaultFactory) {
        this.defaultFactory = defaultFactory;
    }

    public void addAlternativeFactory(final FilteredResourceFactory factory) {
        alternatives.add(factory);
    }

    @Override
    public Optional<FilteredResource<?>> create(final ItemStack stack, final boolean tryAlternatives) {
        if (tryAlternatives) {
            for (final FilteredResourceFactory factory : alternatives) {
                final Optional<FilteredResource<?>> filteredResource = factory.create(stack, true);
                if (filteredResource.isPresent()) {
                    return filteredResource;
                }
            }
        }
        return defaultFactory.create(stack, false);
    }
}
