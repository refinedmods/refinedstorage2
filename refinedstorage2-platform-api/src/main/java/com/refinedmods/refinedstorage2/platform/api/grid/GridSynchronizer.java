package com.refinedmods.refinedstorage2.platform.api.grid;

import net.minecraft.network.chat.Component;

public interface GridSynchronizer {
    Component getTitle();

    void synchronizeFromGrid(String text);

    String getTextToSynchronizeToGrid();
}
