package com.refinedmods.refinedstorage2.platform.apiimpl.grid;

import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.SideButtonWidget;

import net.minecraft.resources.ResourceLocation;

public abstract class DefaultGridSynchronizer implements GridSynchronizer {
    @Override
    public ResourceLocation getTextureIdentifier() {
        return SideButtonWidget.DEFAULT_TEXTURE;
    }

    @Override
    public int getYTexture() {
        return 97;
    }
}
