package com.refinedmods.refinedstorage2.api.grid;

public enum GridSortingDirection {
    ASCENDING,
    DESCENDING;

    public GridSortingDirection toggle() {
        return this == ASCENDING ? DESCENDING : ASCENDING;
    }
}
