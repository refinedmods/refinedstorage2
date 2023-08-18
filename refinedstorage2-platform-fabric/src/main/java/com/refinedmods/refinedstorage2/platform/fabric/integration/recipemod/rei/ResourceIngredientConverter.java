package com.refinedmods.refinedstorage2.platform.fabric.integration.recipemod.rei;

import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import java.util.Optional;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.util.EntryStacks;

public class ResourceIngredientConverter implements IngredientConverter {
    @Override
    public Optional<Object> convertToResource(final Object ingredient) {
        return Optional.empty();
    }

    @Override
    public Optional<Object> convertToIngredient(final Object resource) {
        if (!(resource instanceof ResourceTemplate<?> resourceTemplate)) {
            return Optional.empty();
        }
        if (resourceTemplate.resource() instanceof ItemResource itemResource) {
            return Optional.of(EntryStacks.of(itemResource.toItemStack()));
        }
        if (resourceTemplate.resource() instanceof FluidResource fluidResource) {
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
