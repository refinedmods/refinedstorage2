package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Optional;

import org.apiguardian.api.API;

/**
 * Transforms resources into {@link GridResource}s.
 */
@FunctionalInterface
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.6")
public interface GridResourceFactory {
    /**
     * Transforms a {@link ResourceAmount} into a {@link GridResource}.
     * It's important to keep the {@link ResourceAmount} instance around to
     * get updated resource amounts from the {@link GridView} backing list.
     *
     * @param resourceAmount the resource amount from the backing list
     * @return the grid resource, if applicable
     */
    Optional<GridResource> apply(ResourceAmount resourceAmount);
}
