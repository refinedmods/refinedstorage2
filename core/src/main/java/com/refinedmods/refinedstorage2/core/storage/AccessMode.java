package com.refinedmods.refinedstorage2.core.storage;

public enum AccessMode {
    INSERT_EXTRACT,
    INSERT,
    EXTRACT;

    public AccessMode toggle() {
        switch (this) {
            case INSERT_EXTRACT:
                return INSERT;
            case INSERT:
                return EXTRACT;
            case EXTRACT:
                return INSERT_EXTRACT;
            default:
                return INSERT_EXTRACT;
        }
    }
}
