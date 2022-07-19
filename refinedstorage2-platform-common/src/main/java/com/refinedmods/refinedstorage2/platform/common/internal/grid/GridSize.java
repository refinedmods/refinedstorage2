package com.refinedmods.refinedstorage2.platform.common.internal.grid;

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
