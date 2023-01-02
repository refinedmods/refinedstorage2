package com.refinedmods.refinedstorage2.platform.common.integration.recipemod.jei;

import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.FluidGridResource;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.ItemGridResource;

import java.util.Optional;

import mezz.jei.api.helpers.IPlatformFluidHelper;

public class GridIngredientConverter implements IngredientConverter {
    private final IPlatformFluidHelper<?> fluidHelper;

    public GridIngredientConverter(final IPlatformFluidHelper<?> fluidHelper) {
        this.fluidHelper = fluidHelper;
    }

    @Override
    public Optional<Object> convertToResource(final Object ingredient) {
        return Optional.empty();
    }

    @Override
    public Optional<Object> convertToIngredient(final Object resource) {
        if (resource instanceof ItemGridResource itemResource) {
            return Optional.of(itemResource.getItemStack());
        }
        if (resource instanceof FluidGridResource fluidResource) {
            return Optional.of(fluidHelper.create(fluidResource.getResourceAmount().getResource().fluid(), 1));
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final GridIngredientConverter that = (GridIngredientConverter) o;

        return fluidHelper.equals(that.fluidHelper);
    }

    @Override
    public int hashCode() {
        return fluidHelper.hashCode();
    }
}
