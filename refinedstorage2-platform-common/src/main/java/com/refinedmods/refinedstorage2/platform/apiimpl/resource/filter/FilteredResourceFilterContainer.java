package com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;

import net.minecraft.resources.ResourceLocation;

public class FilteredResourceFilterContainer extends ResourceFilterContainer {
    private final ResourceType<?> allowedType;

    public FilteredResourceFilterContainer(OrderedRegistry<ResourceLocation, ResourceType<?>> resourceTypeRegistry, int size, Runnable listener, ResourceType<?> allowedType) {
        super(resourceTypeRegistry, size, listener);
        this.allowedType = allowedType;
    }

    @Override
    public <T> void set(int slot, ResourceType<T> type, T value) {
        if (type != this.allowedType) {
            return;
        }
        super.set(slot, type, value);
    }

    @Override
    public ResourceType<?> determineDefaultType() {
        return allowedType;
    }
}
