package com.refinedmods.refinedstorage2.platform.api.resource.filter;

import net.minecraft.resources.ResourceLocation;

public interface ResourceTypeRegistry {
    void register(ResourceType<?> resourceType);

    ResourceType<?> get(ResourceLocation id);

    ResourceType<?> getDefault();

    ResourceType<?> toggle(ResourceType<?> resourceType);
}
