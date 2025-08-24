package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Arrays;
import java.util.Optional;

class IngredientState {
    private final long amount;
    private final CraftingState.ResourceState[] possibilities;
    private int pos;

    IngredientState(final Ingredient ingredient, final CraftingState state) {
        this.amount = ingredient.amount();
        this.possibilities = new CraftingState.ResourceState[ingredient.inputs().size()];
        for (int i = 0; i < ingredient.inputs().size(); i++) {
            final ResourceKey resource = ingredient.inputs().get(i);
            possibilities[i] = state.getResource(resource);
        }
        Arrays.sort(possibilities);
    }

    CraftingState.ResourceState get() {
        return possibilities[pos];
    }

    long amount() {
        return amount;
    }

    Optional<CraftingState.ResourceState> cycle() {
        if (pos + 1 >= possibilities.length) {
            return Optional.empty();
        }
        pos++;
        return Optional.of(possibilities[pos]);
    }
}
