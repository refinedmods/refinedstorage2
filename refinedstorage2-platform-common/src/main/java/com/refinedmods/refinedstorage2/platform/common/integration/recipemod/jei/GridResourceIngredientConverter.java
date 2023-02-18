package com.refinedmods.refinedstorage2.platform.common.integration.recipemod.jei;

import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.FluidGridResource;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.ItemGridResource;

import java.util.Optional;

import mezz.jei.api.helpers.IPlatformFluidHelper;

public class GridResourceIngredientConverter implements IngredientConverter {
    private final IPlatformFluidHelper<?> fluidHelper;

    public GridResourceIngredientConverter(final IPlatformFluidHelper<?> fluidHelper) {
        this.fluidHelper = fluidHelper;
    }

    @Override
    public Optional<Object> convertToResource(final Object ingredient) {
        return Optional.empty();
    }

    @Override
    public Optional<Object> convertToIngredient(final Object resource) {
        if (resource instanceof ItemGridResource itemGridResource) {
            return Optional.of(itemGridResource.copyItemStack());
        }
        if (resource instanceof FluidGridResource fluidGridResource) {
            return Optional.of(fluidHelper.create(
                fluidGridResource.getFluidResource().fluid(),
                fluidHelper.bucketVolume(),
                fluidGridResource.getFluidResource().tag()
            ));
        }
        return Optional.empty();
    }
}
