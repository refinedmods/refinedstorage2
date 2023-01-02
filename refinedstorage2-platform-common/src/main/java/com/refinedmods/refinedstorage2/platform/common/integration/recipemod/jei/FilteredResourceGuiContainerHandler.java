package com.refinedmods.refinedstorage2.platform.common.integration.recipemod.jei;

import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.common.screen.AbstractBaseScreen;

import java.util.Optional;
import javax.annotation.Nullable;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;

public class FilteredResourceGuiContainerHandler implements IGuiContainerHandler<AbstractBaseScreen<?>> {
    private final IngredientConverter converter;
    private final IIngredientManager ingredientManager;

    public FilteredResourceGuiContainerHandler(final IngredientConverter converter,
                                               final IIngredientManager ingredientManager) {
        this.converter = converter;
        this.ingredientManager = ingredientManager;
    }

    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(
        final AbstractBaseScreen<?> baseScreen,
        final double mouseX,
        final double mouseY
    ) {
        return convertToIngredient(baseScreen.getFilteredResource())
            .flatMap(this::convertToClickableIngredient);
    }

    public Optional<Object> convertToIngredient(@Nullable final FilteredResource<?> filteredResource) {
        if (filteredResource == null) {
            return Optional.empty();
        }
        return converter.convertToIngredient(filteredResource);
    }

    private Optional<IClickableIngredient<?>> convertToClickableIngredient(final Object ingredient) {
        final IIngredientHelper<Object> helper = ingredientManager.getIngredientHelper(ingredient);
        final Optional<ITypedIngredient<Object>> maybeTypedIngredient =
            ingredientManager.createTypedIngredient(helper.getIngredientType(), ingredient);
        return maybeTypedIngredient.map(typedIngredient -> new ClickableIngredient<>(typedIngredient, 16, 16));
    }
}
