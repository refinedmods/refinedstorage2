package com.refinedmods.refinedstorage2.platform.fabric.api.resource.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

public class ResourceTypeRegistry {
    private final Map<ResourceLocation, ResourceType<?>> map = new HashMap<>();
    private final List<ResourceType<?>> order = new ArrayList<>();
    private final ResourceType<?> defaultType;

    public ResourceTypeRegistry(ResourceType<?> defaultType) {
        register(defaultType);
        this.defaultType = defaultType;
    }

    public void register(ResourceType<?> resourceType) {
        map.put(resourceType.getId(), resourceType);
        order.add(resourceType);
    }

    public ResourceType<?> get(ResourceLocation id) {
        return map.get(id);
    }

    public ResourceType<?> getDefault() {
        return defaultType;
    }

    public ResourceType<?> toggle(ResourceType<?> currentResourceType) {
        int index = order.indexOf(currentResourceType);
        int nextIndex = index + 1;
        if (nextIndex >= order.size()) {
            return order.get(0);
        }
        return order.get(nextIndex);
    }
}
