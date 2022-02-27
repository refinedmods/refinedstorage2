package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public enum GridSortingType {
    QUANTITY(view -> Comparator.comparingLong(a -> a.getResourceAmount().getAmount())),
    NAME(view -> Comparator.comparing((GridResource<?> a) -> a.getName())),
    ID(view -> Comparator.comparingInt(GridResource::getId)),
    LAST_MODIFIED(view -> (a, b) -> {
        Optional<StorageTracker.Entry> trackerEntryA = ((GridView) view).getTrackerEntry(a.getResourceAmount().getResource());
        Optional<StorageTracker.Entry> trackerEntryB = ((GridView) view).getTrackerEntry(b.getResourceAmount().getResource());

        long lastModifiedA = trackerEntryA.map(StorageTracker.Entry::time).orElse(0L);
        long lastModifiedB = trackerEntryB.map(StorageTracker.Entry::time).orElse(0L);

        return Long.compare(lastModifiedA, lastModifiedB);
    });

    private final Function<GridView<?>, Comparator<GridResource<?>>> comparator;

    GridSortingType(Function<GridView<?>, Comparator<GridResource<?>>> comparator) {
        this.comparator = comparator;
    }

    public Function<GridView<?>, Comparator<GridResource<?>>> getComparator() {
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
