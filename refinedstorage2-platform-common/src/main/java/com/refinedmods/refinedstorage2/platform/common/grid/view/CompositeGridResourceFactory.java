package com.refinedmods.refinedstorage2.platform.common.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.grid.view.GridResourceFactory;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;

import java.util.Optional;

public class CompositeGridResourceFactory implements GridResourceFactory {
    private final PlatformRegistry<ResourceType> resourceTypeRegistry;

    public CompositeGridResourceFactory(final PlatformRegistry<ResourceType> resourceTypeRegistry) {
        this.resourceTypeRegistry = resourceTypeRegistry;
    }

    @Override
    public Optional<GridResource> apply(final ResourceAmount resourceAmount) {
        return resourceTypeRegistry.getAll()
            .stream()
            .flatMap(type -> type.toGridResource(resourceAmount).stream())
            .findFirst();
    }
}
