package com.refinedmods.refinedstorage2.api.grid.view;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public enum GridSortingDirection {
    ASCENDING,
    DESCENDING;

    public GridSortingDirection toggle() {
        return this == ASCENDING ? DESCENDING : ASCENDING;
    }
}
