package com.refinedmods.refinedstorage2.core.grid;

public enum GridExtractMode {
    CURSOR_STACK,
    CURSOR_HALF,
    PLAYER_INVENTORY_STACK;

    public boolean isCursorLike() {
        return this == CURSOR_STACK || this == CURSOR_HALF;
    }
}
