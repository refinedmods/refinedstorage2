package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Arrays;
import java.util.Optional;

class IngredientState {
    private final long amount;
    private final ResourceKey[] possibilities;
    private int pos;

    IngredientState(final Ingredient ingredient, final CraftingState state) {
        this.amount = ingredient.amount();
        this.possibilities = new ResourceKey[ingredient.inputs().size()];
        for (int i = 0; i < ingredient.inputs().size(); i++) {
            final ResourceKey resource = ingredient.inputs().get(i);
            possibilities[i] = resource;
        }
        Arrays.sort(possibilities, state.storageSorter());
        Arrays.sort(possibilities, state.internalStorageSorter());
    }

    ResourceKey get() {
        return possibilities[pos];
    }

    long amount() {
        return amount;
    }

    Optional<ResourceKey> cycle() {
        if (pos + 1 >= possibilities.length) {
            return Optional.empty();
        }
        pos++;
        return Optional.of(get());
    }
}
