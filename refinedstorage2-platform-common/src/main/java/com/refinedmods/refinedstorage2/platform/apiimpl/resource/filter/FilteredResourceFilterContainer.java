package com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;

import net.minecraft.resources.ResourceLocation;

public class FilteredResourceFilterContainer extends ResourceFilterContainer {
    private final ResourceType allowedType;

    public FilteredResourceFilterContainer(final OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry,
                                           final int size,
                                           final Runnable listener,
                                           final ResourceType allowedType) {
        super(resourceTypeRegistry, size, listener);
        this.allowedType = allowedType;
    }

    @Override
    public void set(final int index, final FilteredResource resource) {
        if (resource.getType() != allowedType) {
            return;
        }
        super.set(index, resource);
    }

    @Override
    public ResourceType determineDefaultType() {
        return allowedType;
    }
}
