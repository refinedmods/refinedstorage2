package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;

import net.minecraft.resources.ResourceLocation;

public abstract class AbstractGridSynchronizer implements GridSynchronizer {
    @Override
    public ResourceLocation getTextureIdentifier() {
        return AbstractSideButtonWidget.DEFAULT_TEXTURE;
    }

    @Override
    public int getYTexture() {
        return 97;
    }
}
