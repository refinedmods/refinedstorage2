package com.refinedmods.refinedstorage2.api.grid.view;

public enum GridSortingDirection {
    ASCENDING,
    DESCENDING;

    public GridSortingDirection toggle() {
        return this == ASCENDING ? DESCENDING : ASCENDING;
    }
}
