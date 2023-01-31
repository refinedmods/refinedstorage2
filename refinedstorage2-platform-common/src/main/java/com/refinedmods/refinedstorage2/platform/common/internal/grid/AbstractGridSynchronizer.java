package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.common.screen.TextureIds;

import net.minecraft.resources.ResourceLocation;

public abstract class AbstractGridSynchronizer implements GridSynchronizer {
    @Override
    public ResourceLocation getTextureIdentifier() {
        return TextureIds.ICONS;
    }

    @Override
    public int getYTexture() {
        return 97;
    }
}
