package com.refinedmods.refinedstorage2.platform.common.integration.recipemod.jei;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.AbstractGridScreen;

import java.util.Optional;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;

public class GridGuiContainerHandler implements IGuiContainerHandler<AbstractGridScreen<?>> {
    private final IngredientConverter converter;
    private final IIngredientManager ingredientManager;

    public GridGuiContainerHandler(final IngredientConverter converter, final IIngredientManager ingredientManager) {
        this.converter = converter;
        this.ingredientManager = ingredientManager;
    }

    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(
        final AbstractGridScreen screen,
        final double mouseX,
        final double mouseY
    ) {
        final GridResource resource = screen.getHoveredResource();
        if (resource == null) {
            return Optional.empty();
        }
        return converter
            .convertToIngredient(resource)
            .flatMap(ingredient -> convertToClickableIngredient(mouseX, mouseY, ingredient));
    }

    private Optional<IClickableIngredient<?>> convertToClickableIngredient(final double x,
                                                                           final double y,
                                                                           final Object ingredient) {
        final IIngredientHelper<Object> helper = ingredientManager.getIngredientHelper(ingredient);
        final Optional<ITypedIngredient<Object>> maybeTypedIngredient =
            ingredientManager.createTypedIngredient(helper.getIngredientType(), ingredient);
        return maybeTypedIngredient
            .map(typedIngredient -> new ClickableIngredient<>(typedIngredient, (int) x, (int) y));
    }
}
