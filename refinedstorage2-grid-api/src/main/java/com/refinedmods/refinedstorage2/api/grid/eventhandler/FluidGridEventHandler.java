package com.refinedmods.refinedstorage2.api.grid.eventhandler;

public interface FluidGridEventHandler {
    void onInsertFromCursor();

    void onActiveChanged(boolean active);
}
