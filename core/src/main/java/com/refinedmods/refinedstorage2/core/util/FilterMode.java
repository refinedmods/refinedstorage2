package com.refinedmods.refinedstorage2.core.util;

public enum FilterMode {
    ALLOW,
    BLOCK;

    public FilterMode toggle() {
        return this == ALLOW ? BLOCK : ALLOW;
    }
}
