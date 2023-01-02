package com.refinedmods.refinedstorage2.platform.common.integration.recipemod;

import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;

import java.util.Collection;
import java.util.Optional;

public class CompositeIngredientConverter implements IngredientConverter {
    private final Collection<IngredientConverter> converters;

    public CompositeIngredientConverter(final Collection<IngredientConverter> converters) {
        this.converters = converters;
    }

    @Override
    public Optional<Object> convertToResource(final Object ingredient) {
        return converters.stream()
                .flatMap(conv -> conv.convertToResource(ingredient).stream())
                .findFirst();
    }

    @Override
    public Optional<Object> convertToIngredient(final Object resource) {
        return converters.stream()
                .flatMap(conv -> conv.convertToIngredient(resource).stream())
                .findFirst();
    }
}
