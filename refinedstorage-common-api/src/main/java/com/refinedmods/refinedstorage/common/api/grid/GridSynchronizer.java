package com.refinedmods.refinedstorage.common.api.grid;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface GridSynchronizer {
    MutableComponent getTitle();

    Component getHelpText();

    void synchronizeFromGrid(String text);

    @Nullable
    String getTextToSynchronizeToGrid();

    Identifier getSprite();
}
