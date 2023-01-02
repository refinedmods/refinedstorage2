package com.refinedmods.refinedstorage2.platform.common.integration.jei;

import com.refinedmods.refinedstorage2.api.grid.view.AbstractGridResource;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.AbstractGridScreen;

import javax.annotation.Nullable;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;

public class GridGuiContainerHandler
    implements IGuiContainerHandler<AbstractGridScreen<?, ? extends AbstractGridContainerMenu<?>>> {
    private final IngredientConverter converter;

    public GridGuiContainerHandler(final IngredientConverter converter) {
        this.converter = converter;
    }

    @Override
    @Nullable
    public Object getIngredientUnderMouse(
        final AbstractGridScreen<?, ? extends AbstractGridContainerMenu<?>> containerScreen,
        final double mouseX,
        final double mouseY
    ) {
        final AbstractGridResource<?> resource = containerScreen.getCurrentlyHoveredResource();
        if (resource == null) {
            return null;
        }
        return converter.convertToIngredient(resource).orElse(null);
    }
}
