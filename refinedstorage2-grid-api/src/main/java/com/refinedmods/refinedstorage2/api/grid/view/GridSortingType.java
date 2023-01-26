package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Comparator;
import java.util.function.Function;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public enum GridSortingType {
    QUANTITY(view -> Comparator.comparingLong(GridResource::getAmount)),
    NAME(view -> Comparator.comparing(GridResource::getName)),
    ID(view -> Comparator.comparingInt(GridResource::getId)),
    LAST_MODIFIED(view -> (a, b) -> {
        final long lastModifiedA = a.getTrackedResource(view).map(TrackedResource::getTime).orElse(0L);
        final long lastModifiedB = b.getTrackedResource(view).map(TrackedResource::getTime).orElse(0L);
        return Long.compare(lastModifiedA, lastModifiedB);
    });

    private final Function<GridView, Comparator<GridResource>> comparator;

    GridSortingType(final Function<GridView, Comparator<GridResource>> comparator) {
        this.comparator = comparator;
    }

    public Function<GridView, Comparator<GridResource>> getComparator() {
        return comparator;
    }
}
