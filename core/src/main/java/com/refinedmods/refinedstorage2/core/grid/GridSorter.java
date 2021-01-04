package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.storage.StorageTracker;

import java.util.Comparator;

public enum GridSorter {
    QUANTITY((a, b) -> Integer.compare(a.getCount(), b.getCount())),
    NAME((a, b) -> a.getName().compareTo(b.getName())),
    ID((a, b) -> Integer.compare(a.getId(), b.getId()));

    private final Comparator<GridStack<?>> comparator;

    GridSorter(Comparator<GridStack<?>> comparator) {
        this.comparator = comparator;
    }

    public Comparator<GridStack<?>> getComparator() {
        return comparator;
    }

    @SuppressWarnings("unchecked")
    public static <T> Comparator<GridStack<?>> getLastModified(GridView<T> view) {
        return (a, b) -> {
            long lastModifiedA = view.getTrackerEntry((T) a.getStack()).map(StorageTracker.Entry::getTime).orElse(0L);
            long lastModifiedB = view.getTrackerEntry((T) b.getStack()).map(StorageTracker.Entry::getTime).orElse(0L);

            if (lastModifiedA != lastModifiedB) {
                return Long.compare(lastModifiedA, lastModifiedB);
            }

            return 0;
        };
    }
}
