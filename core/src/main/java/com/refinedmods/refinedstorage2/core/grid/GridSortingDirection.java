package com.refinedmods.refinedstorage2.core.grid;

public enum GridSortingDirection {
    ASCENDING,
    DESCENDING;

    public GridSortingDirection toggle() {
        return this == ASCENDING ? DESCENDING : ASCENDING;
    }
}
