package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;

import java.util.Comparator;
import java.util.function.Function;

public enum GridSortingType {
    QUANTITY(view -> (a, b) -> Long.compare(a.getAmount(), b.getAmount())),
    NAME(view -> (a, b) -> a.getName().compareTo(b.getName())),
    ID(view -> (a, b) -> Integer.compare(a.getId(), b.getId())),
    LAST_MODIFIED(view -> (a, b) -> {
        long lastModifiedA = view.getTrackerEntry(a.getStack()).map(StorageTracker.Entry::getTime).orElse(0L);
        long lastModifiedB = view.getTrackerEntry(b.getStack()).map(StorageTracker.Entry::getTime).orElse(0L);
        return Long.compare(lastModifiedA, lastModifiedB);
    });

    private final Function<GridView<?>, Comparator<GridStack<?>>> comparator;

    GridSortingType(Function<GridView<?>, Comparator<GridStack<?>>> comparator) {
        this.comparator = comparator;
    }

    public Function<GridView<?>, Comparator<GridStack<?>>> getComparator() {
        return comparator;
    }

    public GridSortingType toggle() {
        return switch (this) {
            case QUANTITY -> NAME;
            case NAME -> ID;
            case ID -> LAST_MODIFIED;
            case LAST_MODIFIED -> QUANTITY;
            default -> QUANTITY;
        };
    }
}
