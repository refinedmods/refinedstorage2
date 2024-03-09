package com.refinedmods.refinedstorage2.platform.common.recipemod;

import com.refinedmods.refinedstorage2.platform.api.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public class CompositeIngredientConverter implements IngredientConverter {
    private final Collection<IngredientConverter> converters = new HashSet<>();

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

    public void addConverter(final IngredientConverter converter) {
        this.converters.add(converter);
    }
}
