package com.refinedmods.refinedstorage2.api.storage;

public enum AccessMode {
    INSERT_EXTRACT,
    INSERT,
    EXTRACT;

    public AccessMode toggle() {
        return switch (this) {
            case INSERT_EXTRACT -> INSERT;
            case INSERT -> EXTRACT;
            case EXTRACT -> INSERT_EXTRACT;
        };
    }
}
