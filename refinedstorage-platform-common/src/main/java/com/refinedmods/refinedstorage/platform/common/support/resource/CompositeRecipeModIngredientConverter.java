package com.refinedmods.refinedstorage.platform.common.support.resource;

import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.api.support.resource.RecipeModIngredientConverter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public class CompositeRecipeModIngredientConverter implements RecipeModIngredientConverter {
    private final Collection<RecipeModIngredientConverter> converters = new HashSet<>();

    @Override
    public Optional<PlatformResourceKey> convertToResource(final Object ingredient) {
        return converters.stream()
            .flatMap(converter -> converter.convertToResource(ingredient).stream())
            .findFirst();
    }

    @Override
    public Optional<Object> convertToIngredient(final PlatformResourceKey resource) {
        return converters.stream()
            .flatMap(converter -> converter.convertToIngredient(resource).stream())
            .findFirst();
    }

    public void addConverter(final RecipeModIngredientConverter converter) {
        this.converters.add(converter);
    }
}
