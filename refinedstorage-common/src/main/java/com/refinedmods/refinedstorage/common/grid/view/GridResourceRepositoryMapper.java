package com.refinedmods.refinedstorage.common.grid.view;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryMapper;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResourceType;
import com.refinedmods.refinedstorage.common.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public class GridResourceRepositoryMapper implements ResourceRepositoryMapper<GridResource> {
    private final PlatformRegistry<GridResourceType> registry;
    @Nullable
    private Map<Class<? extends ResourceKey>, GridResourceType> lazyStrategies;
    @Nullable
    private GridResourceType lazyItemFactory;
    @Nullable
    private GridResourceType lazyFluidFactory;

    public GridResourceRepositoryMapper(final PlatformRegistry<GridResourceType> registry) {
        this.registry = registry;
    }

    private void initializeStrategies() {
        lazyStrategies = registry.getAll().stream()
            .collect(Collectors.toMap(GridResourceType::getResourceKeyClass, strategy -> strategy));
        lazyItemFactory = lazyStrategies.get(ItemResource.class);
        lazyFluidFactory = lazyStrategies.get(FluidResource.class);
    }

    @Override
    public GridResource apply(final ResourceKey resource) {
        if (lazyStrategies == null) {
            initializeStrategies();
        }
        final Class<? extends ResourceKey> resourceClass = resource.getClass();
        if (resourceClass == ItemResource.class && lazyItemFactory != null) {
            return lazyItemFactory.apply(resource);
        } else if (resourceClass == FluidResource.class && lazyFluidFactory != null) {
            return lazyFluidFactory.apply(resource);
        }
        final GridResourceType factory = requireNonNull(lazyStrategies.get(resourceClass),
            "No factory for " + resourceClass);
        return factory.apply(resource);
    }
}
