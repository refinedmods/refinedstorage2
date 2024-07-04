package com.refinedmods.refinedstorage.platform.common.support.stretching;

public enum ScreenSize {
    STRETCH,
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE;

    public ScreenSize toggle() {
        return switch (this) {
            case STRETCH -> SMALL;
            case SMALL -> MEDIUM;
            case MEDIUM -> LARGE;
            case LARGE -> EXTRA_LARGE;
            case EXTRA_LARGE -> STRETCH;
        };
    }
}
