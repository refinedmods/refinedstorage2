package com.refinedmods.refinedstorage2.platform.fabric.recipemod.rei;

import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.platform.api.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.grid.view.FluidGridResource;
import com.refinedmods.refinedstorage2.platform.common.grid.view.ItemGridResource;

import java.util.Optional;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.util.EntryStacks;

class GridResourceIngredientConverter implements IngredientConverter {
    @Override
    public Optional<ResourceTemplate> convertToResource(final Object ingredient) {
        return Optional.empty();
    }

    @Override
    public Optional<Object> convertToIngredient(final Object resource) {
        if (resource instanceof ItemGridResource itemGridResource) {
            return Optional.of(EntryStacks.of(itemGridResource.copyItemStack()));
        }
        if (resource instanceof FluidGridResource fluidGridResource) {
            final FluidResource fluidResource = (FluidResource) fluidGridResource.getResource();
            final FluidStack fluidStack = FluidStack.create(
                fluidResource.fluid(),
                FluidStack.bucketAmount(),
                fluidResource.tag()
            );
            return Optional.of(EntryStacks.of(fluidStack));
        }
        return Optional.empty();
    }
}
