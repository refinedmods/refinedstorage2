package com.refinedmods.refinedstorage2.core.grid;

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
}
