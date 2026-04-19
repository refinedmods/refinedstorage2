package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepository;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;

import java.util.Comparator;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

public enum GridSortingTypes {
    QUANTITY(trp -> view -> Comparator.comparingLong(value -> value.getAmount(view))),
    NAME(trp -> view -> Comparator.comparing(GridResource::getHoverName)),
    ID(trp -> view -> Comparator.comparingInt(GridResource::getRegistryId)),
    LAST_MODIFIED(trp -> view -> (a, b) -> {
        final long lastModifiedA = extractTime(trp.getTrackedResource(a));
        final long lastModifiedB = extractTime(trp.getTrackedResource(b));
        return Long.compare(lastModifiedA, lastModifiedB);
    });

    private final Function<TrackedResourceProvider, Function<ResourceRepository<GridResource>,
        Comparator<GridResource>>> comparator;

    GridSortingTypes(
        final Function<TrackedResourceProvider, Function<ResourceRepository<GridResource>, Comparator<GridResource>>> c
    ) {
        this.comparator = c;
    }

    public Function<ResourceRepository<GridResource>, Comparator<GridResource>> apply(
        final TrackedResourceProvider context
    ) {
        return comparator.apply(context);
    }

    @FunctionalInterface
    public interface TrackedResourceProvider {
        @Nullable
        TrackedResource getTrackedResource(GridResource resource);
    }

    private static long extractTime(@Nullable final TrackedResource trackedResource) {
        return trackedResource != null ? trackedResource.getTime() : 0;
    }
}
