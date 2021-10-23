package com.refinedmods.refinedstorage2.api.grid.view;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public enum GridSize {
    STRETCH,
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE;

    public GridSize toggle() {
        return switch (this) {
            case STRETCH -> SMALL;
            case SMALL -> MEDIUM;
            case MEDIUM -> LARGE;
            case LARGE -> EXTRA_LARGE;
            case EXTRA_LARGE -> STRETCH;
        };
    }
}
