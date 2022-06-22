package com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceTypeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

public class ResourceTypeRegistryImpl implements ResourceTypeRegistry {
    private final Map<ResourceLocation, ResourceType<?>> map = new HashMap<>();
    private final List<ResourceType<?>> order = new ArrayList<>();
    private final ResourceType<?> defaultType;

    public ResourceTypeRegistryImpl(ResourceType<?> defaultType) {
        this.register(defaultType);
        this.defaultType = defaultType;
    }

    @Override
    public void register(ResourceType<?> resourceType) {
        if (map.containsKey(resourceType.getId()) || order.contains(resourceType)) {
            throw new IllegalArgumentException("Resource type already registered!");
        }
        map.put(resourceType.getId(), resourceType);
        order.add(resourceType);
    }

    @Override
    public ResourceType<?> get(ResourceLocation id) {
        return map.get(id);
    }

    @Override
    public ResourceType<?> getDefault() {
        return defaultType;
    }

    @Override
    public ResourceType<?> toggle(ResourceType<?> resourceType) {
        int index = order.indexOf(resourceType);
        int nextIndex = index + 1;
        if (nextIndex >= order.size()) {
            return order.get(0);
        }
        return order.get(nextIndex);
    }
}
