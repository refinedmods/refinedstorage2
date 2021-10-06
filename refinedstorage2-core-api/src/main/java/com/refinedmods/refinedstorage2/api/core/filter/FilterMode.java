package com.refinedmods.refinedstorage2.api.core.filter;

public enum FilterMode {
    ALLOW,
    BLOCK;

    public FilterMode toggle() {
        return this == ALLOW ? BLOCK : ALLOW;
    }
}
