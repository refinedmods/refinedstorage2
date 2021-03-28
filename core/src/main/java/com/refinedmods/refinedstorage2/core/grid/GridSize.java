package com.refinedmods.refinedstorage2.core.grid;

public enum GridSize {
    STRETCH,
    SMALL,
    MEDIUM,
    LARGE;

    public GridSize toggle() {
        switch (this) {
            case STRETCH:
                return SMALL;
            case SMALL:
                return MEDIUM;
            case MEDIUM:
                return LARGE;
            case LARGE:
                return STRETCH;
            default:
                return STRETCH;
        }
    }
}
