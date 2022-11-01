package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;

import net.minecraft.resources.ResourceLocation;

public class FilteredResourceFilterContainer extends ResourceFilterContainer {
    private final ResourceType allowedType;

    public FilteredResourceFilterContainer(final OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry,
                                           final int size,
                                           final ResourceType allowedType) {
        this(resourceTypeRegistry, size, () -> {
        }, allowedType, -1);
    }

    public FilteredResourceFilterContainer(final OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry,
                                           final int size,
                                           final ResourceType allowedType,
                                           final long maxAmount) {
        this(resourceTypeRegistry, size, () -> {
        }, allowedType, maxAmount);
    }

    public FilteredResourceFilterContainer(final OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry,
                                           final int size,
                                           final Runnable listener,
                                           final ResourceType allowedType) {
        this(resourceTypeRegistry, size, listener, allowedType, -1);
    }

    public FilteredResourceFilterContainer(final OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry,
                                           final int size,
                                           final Runnable listener,
                                           final ResourceType allowedType,
                                           final long maxAmount) {
        super(resourceTypeRegistry, size, listener, maxAmount);
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
