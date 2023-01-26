package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;

/**
 * Represents a resource in the grid.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface GridResource {
    Optional<TrackedResource> getTrackedResource(GridView view);

    long getAmount();

    int getId();

    String getName();

    Set<String> getAttribute(GridResourceAttributeKey key);

    boolean isZeroed();

    void setZeroed(boolean zeroed);
}
