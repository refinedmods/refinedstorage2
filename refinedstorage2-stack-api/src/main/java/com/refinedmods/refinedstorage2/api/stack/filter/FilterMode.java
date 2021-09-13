package com.refinedmods.refinedstorage2.api.stack.filter;

// TODO: Move to core package.
public enum FilterMode {
    ALLOW,
    BLOCK;

    public FilterMode toggle() {
        return this == ALLOW ? BLOCK : ALLOW;
    }
}
