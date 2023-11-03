package com.refinedmods.refinedstorage2.platform.forge.recipemod.rei;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

class MissingIngredients {
    private final List<EntryIngredient> ingredients = new ArrayList<>();
    private final IntSet indices = new IntOpenHashSet();

    boolean isEmpty() {
        return indices.isEmpty();
    }

    void addIngredient(final EntryIngredient ingredient, final int slotIndex) {
        ingredients.add(ingredient);
        indices.add(slotIndex);
    }

    List<EntryIngredient> getIngredients() {
        return ingredients;
    }

    boolean isMissing(final int slotIndex) {
        return indices.contains(slotIndex);
    }
}
