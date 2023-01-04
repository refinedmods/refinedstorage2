package com.refinedmods.refinedstorage2.platform.forge.integration.recipemod.rei;

import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;

import java.util.Optional;

import me.shedaniel.rei.api.common.util.EntryStacks;

public class FilterIngredientConverter implements IngredientConverter {

    @Override
    public Optional<Object> convertToResource(final Object ingredient) {
        return Optional.empty();
    }

    @Override
    public Optional<Object> convertToIngredient(final Object resource) {
        if (resource instanceof FilteredResource filteredResource) {
            if (filteredResource.getValue() instanceof ItemResource itemResource) {
                return Optional.of(EntryStacks.of(itemResource.toItemStack()));
            }
            if (filteredResource.getValue() instanceof FluidResource fluidResource) {
                return Optional.of(EntryStacks.of((fluidResource.fluid())));
            }
        }
        return Optional.empty();
    }
}
