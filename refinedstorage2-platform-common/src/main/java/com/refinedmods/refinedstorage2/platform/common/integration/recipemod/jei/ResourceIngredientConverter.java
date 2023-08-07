package com.refinedmods.refinedstorage2.platform.common.integration.recipemod.jei;

import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceInstance;

import java.util.Optional;

import mezz.jei.api.helpers.IPlatformFluidHelper;

public class ResourceIngredientConverter implements IngredientConverter {
    private final IPlatformFluidHelper<?> fluidHelper;

    public ResourceIngredientConverter(final IPlatformFluidHelper<?> fluidHelper) {
        this.fluidHelper = fluidHelper;
    }

    @Override
    public Optional<Object> convertToResource(final Object ingredient) {
        return Optional.empty();
    }

    @Override
    public Optional<Object> convertToIngredient(final Object resource) {
        if (!(resource instanceof ResourceInstance<?> resourceInstance)) {
            return Optional.empty();
        }
        if (resourceInstance.getResource() instanceof ItemResource itemResource) {
            return Optional.of(itemResource.toItemStack());
        }
        if (resourceInstance.getResource() instanceof FluidResource fluidResource) {
            return Optional.of(fluidHelper.create(
                fluidResource.fluid(),
                fluidHelper.bucketVolume(),
                fluidResource.tag()
            ));
        }
        return Optional.empty();
    }
}
