package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public enum GridSortingType {
    QUANTITY(view -> Comparator.comparingLong(a -> a.getResourceAmount().getAmount())),
    NAME(view -> Comparator.comparing(AbstractGridResource::getName)),
    ID(view -> Comparator.comparingInt(AbstractGridResource::getId)),
    LAST_MODIFIED(view -> (a, b) -> {
        final Optional<TrackedResource> trackedA = view.getTrackedResource(a.getResourceAmount().getResource());
        final Optional<TrackedResource> trackedB = view.getTrackedResource(b.getResourceAmount().getResource());
        final long lastModifiedA = trackedA.map(TrackedResource::getTime).orElse(0L);
        final long lastModifiedB = trackedB.map(TrackedResource::getTime).orElse(0L);
        return Long.compare(lastModifiedA, lastModifiedB);
    });

    private final Function<GridView, Comparator<AbstractGridResource>> comparator;

    GridSortingType(final Function<GridView, Comparator<AbstractGridResource>> comparator) {
        this.comparator = comparator;
    }

    public Function<GridView, Comparator<AbstractGridResource>> getComparator() {
        return comparator;
    }
}
