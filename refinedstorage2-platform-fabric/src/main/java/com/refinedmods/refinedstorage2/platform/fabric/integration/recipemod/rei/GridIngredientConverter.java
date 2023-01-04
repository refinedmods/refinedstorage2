package com.refinedmods.refinedstorage2.platform.fabric.integration.recipemod.rei;

import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.FluidGridResource;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.ItemGridResource;

import java.util.Optional;

import me.shedaniel.rei.api.common.util.EntryStacks;

public class GridIngredientConverter implements IngredientConverter {
    @Override
    public Optional<Object> convertToResource(final Object ingredient) {
        return Optional.empty();
    }

    @Override
    public Optional<Object> convertToIngredient(final Object resource) {
        if (resource instanceof ItemGridResource itemResource) {
            return Optional.of(EntryStacks.of(itemResource.getItemStack()));
        }
        if (resource instanceof FluidGridResource fluidResource) {
            return Optional.of(EntryStacks.of(fluidResource.getResourceAmount().getResource().fluid()));
        }
        return Optional.empty();
    }
}
