package com.refinedmods.refinedstorage2.platform.common.integration.recipemod;

import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public class CompositeIngredientConverter implements IngredientConverter {
    private final Collection<IngredientConverter> converters = new HashSet<>();

    @Override
    public Optional<Object> convertToResource(final Object ingredient) {
        return converters.stream()
                .flatMap(converter -> converter.convertToResource(ingredient).stream())
                .findFirst();
    }

    @Override
    public Optional<Object> convertToIngredient(final Object resource) {
        return converters.stream()
                .flatMap(converter -> converter.convertToIngredient(resource).stream())
                .findFirst();
    }

    public void addConverter(final IngredientConverter converter) {
        this.converters.add(converter);
    }
}
