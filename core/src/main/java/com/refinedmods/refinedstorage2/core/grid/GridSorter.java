package com.refinedmods.refinedstorage2.core.grid;

import java.util.Comparator;
import java.util.function.Function;

import com.refinedmods.refinedstorage2.core.storage.StorageTracker;

public enum GridSorter {
    QUANTITY((view) -> (a, b) -> Integer.compare(a.getCount(), b.getCount())),
    NAME((view) -> (a, b) -> a.getName().compareTo(b.getName())),
    ID((view) -> (a, b) -> Integer.compare(a.getId(), b.getId())),
    LAST_MODIFIED((view) -> (a, b) -> {
        long lastModifiedA = view.getTrackerEntry(a.getStack()).map(StorageTracker.Entry::getTime).orElse(0L);
        long lastModifiedB = view.getTrackerEntry(b.getStack()).map(StorageTracker.Entry::getTime).orElse(0L);

        if (lastModifiedA != lastModifiedB) {
            return Long.compare(lastModifiedA, lastModifiedB);
        }

        return 0;
    });

    private final Function<GridView<?>, Comparator<GridStack<?>>> comparator;

    GridSorter(Function<GridView<?>, Comparator<GridStack<?>>> comparator) {
        this.comparator = comparator;
    }

    public Function<GridView<?>, Comparator<GridStack<?>>> getComparator() {
        return comparator;
    }

    public GridSorter toggle() {
        switch (this) {
            case QUANTITY:
                return NAME;
            case NAME:
                return ID;
            case ID:
                return LAST_MODIFIED;
            case LAST_MODIFIED:
                return QUANTITY;
            default:
                return QUANTITY;
        }
    }
}
