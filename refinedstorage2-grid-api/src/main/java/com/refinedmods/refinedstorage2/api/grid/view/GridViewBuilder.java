package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

/**
 * Constructs a grid view, based on an initial set of resources.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public interface GridViewBuilder {
    /**
     * Adds a resource in the backing and view list.
     *
     * @param resource        the resource
     * @param amount          the amount
     * @param trackedResource the tracked resource, can be null
     * @return this builder
     */
    GridViewBuilder withResource(ResourceKey resource, long amount, @Nullable TrackedResource trackedResource);

    /**
     * @return a {@link GridView} with the specified resources
     */
    GridView build();
}
