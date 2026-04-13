package com.refinedmods.refinedstorage.common.grid.view;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryMapper;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public class GridResourceRepositoryMapper implements ResourceRepositoryMapper<GridResource> {
    private final Map<Class<? extends ResourceKey>, ResourceRepositoryMapper<GridResource>> strategies =
        new HashMap<>();
    @Nullable
    private ResourceRepositoryMapper<GridResource> itemFactory;
    @Nullable
    private ResourceRepositoryMapper<GridResource> fluidFactory;

    public void addFactory(final Class<? extends ResourceKey> resourceClass,
                           final ResourceRepositoryMapper<GridResource> factory) {
        if (resourceClass == ItemResource.class) {
            this.itemFactory = factory;
        } else if (resourceClass == FluidResource.class) {
            this.fluidFactory = factory;
        } else {
            this.strategies.put(resourceClass, factory);
        }
    }

    @Override
    public GridResource apply(final ResourceKey resource) {
        final Class<? extends ResourceKey> resourceClass = resource.getClass();
        if (resourceClass == ItemResource.class && itemFactory != null) {
            return itemFactory.apply(resource);
        } else if (resourceClass == FluidResource.class && fluidFactory != null) {
            return fluidFactory.apply(resource);
        }
        final ResourceRepositoryMapper<GridResource> factory = requireNonNull(
            strategies.get(resourceClass),
            "No factory for " + resourceClass
        );
        return factory.apply(resource);
    }
}
