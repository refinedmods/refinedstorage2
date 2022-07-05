package com.refinedmods.refinedstorage2.platform.api.grid;

import javax.annotation.Nullable;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public interface GridSynchronizer {
    MutableComponent getTitle();

    void synchronizeFromGrid(String text);

    @Nullable
    String getTextToSynchronizeToGrid();

    ResourceLocation getTextureIdentifier();

    int getXTexture();

    int getYTexture();
}
