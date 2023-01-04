package com.refinedmods.refinedstorage2.platform.forge.integration.recipemod.rei;

import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.FluidGridResource;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.ItemGridResource;

import java.util.Optional;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.util.EntryStacks;

public class GridResourceIngredientConverter implements IngredientConverter {
    @Override
    public Optional<Object> convertToResource(final Object ingredient) {
        return Optional.empty();
    }

    @Override
    public Optional<Object> convertToIngredient(final Object resource) {
        if (resource instanceof ItemGridResource itemResource) {
            return Optional.of(EntryStacks.of(itemResource.copyItemStack()));
        }
        if (resource instanceof FluidGridResource fluidResource) {
            final FluidStack fluidStack = FluidStack.create(
                fluidResource.getFluidResource().fluid(),
                1,
                FluidResource.toTag(fluidResource.getFluidResource())
            );
            return Optional.of(EntryStacks.of(fluidStack));
        }
        return Optional.empty();
    }
}
