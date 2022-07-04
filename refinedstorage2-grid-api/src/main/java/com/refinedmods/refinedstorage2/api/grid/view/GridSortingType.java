package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public enum GridSortingType {
    QUANTITY(view -> Comparator.comparingLong(a -> a.getResourceAmount().getAmount())),
    NAME(view -> Comparator.comparing((AbstractGridResource<?> a) -> a.getName())),
    ID(view -> Comparator.comparingInt(AbstractGridResource::getId)),
    @SuppressWarnings({"unchecked", "rawtypes"})
    LAST_MODIFIED(view -> (a, b) -> {
        final Optional<TrackedResource> trackedA = ((GridView) view)
            .getTrackedResource(a.getResourceAmount().getResource());
        final Optional<TrackedResource> trackedB = ((GridView) view)
            .getTrackedResource(b.getResourceAmount().getResource());

        final long lastModifiedA = trackedA.map(TrackedResource::getTime).orElse(0L);
        final long lastModifiedB = trackedB.map(TrackedResource::getTime).orElse(0L);

        return Long.compare(lastModifiedA, lastModifiedB);
    });

    private final Function<GridView<?>, Comparator<AbstractGridResource<?>>> comparator;

    GridSortingType(final Function<GridView<?>, Comparator<AbstractGridResource<?>>> comparator) {
        this.comparator = comparator;
    }

    public Function<GridView<?>, Comparator<AbstractGridResource<?>>> getComparator() {
        return comparator;
    }

    public GridSortingType toggle() {
        return switch (this) {
            case QUANTITY -> NAME;
            case NAME -> ID;
            case ID -> LAST_MODIFIED;
            case LAST_MODIFIED -> QUANTITY;
        };
    }
}
