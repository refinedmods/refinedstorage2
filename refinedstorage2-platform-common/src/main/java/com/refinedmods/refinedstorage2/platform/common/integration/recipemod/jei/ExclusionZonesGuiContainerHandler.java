package com.refinedmods.refinedstorage2.platform.common.integration.recipemod.jei;

import com.refinedmods.refinedstorage2.platform.common.screen.AbstractBaseScreen;

import java.util.List;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;

public class ExclusionZonesGuiContainerHandler implements IGuiContainerHandler<AbstractBaseScreen<?>> {
    @Override
    public List<Rect2i> getGuiExtraAreas(final AbstractBaseScreen<?> screen) {
        return screen.getExclusionZones();
    }
}
