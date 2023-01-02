package com.refinedmods.refinedstorage2.platform.common.integration.recipemod.jei;

import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;

import java.util.Optional;

import mezz.jei.api.helpers.IPlatformFluidHelper;

public class FilterIngredientConverter implements IngredientConverter {
    private final IPlatformFluidHelper<?> fluidHelper;

    public FilterIngredientConverter(final IPlatformFluidHelper<?> fluidHelper) {
        this.fluidHelper = fluidHelper;
    }

    @Override
    public Optional<Object> convertToResource(final Object ingredient) {
        return Optional.empty();
    }

    @Override
    public Optional<Object> convertToIngredient(final Object resource) {
        if (resource instanceof FilteredResource filteredResource) {
            if (filteredResource.getValue() instanceof ItemResource itemResource) {
                return Optional.of(itemResource.toItemStack());
            }
            if (filteredResource.getValue() instanceof FluidResource fluidResource) {
                return Optional.of(fluidHelper.create(fluidResource.fluid(), 1));
            }
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

        final FilterIngredientConverter that = (FilterIngredientConverter) o;

        return fluidHelper.equals(that.fluidHelper);
    }

    @Override
    public int hashCode() {
        return fluidHelper.hashCode();
    }
}
