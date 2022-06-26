package com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;

import net.minecraft.resources.ResourceLocation;

public class FilteredResourceFilterContainer extends ResourceFilterContainer {
    private final ResourceType allowedType;

    public FilteredResourceFilterContainer(OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry, int size, Runnable listener, ResourceType allowedType) {
        super(resourceTypeRegistry, size, listener);
        this.allowedType = allowedType;
    }

    @Override
    public void set(int index, FilteredResource resource) {
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
