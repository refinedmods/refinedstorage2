package com.refinedmods.refinedstorage.api.autocrafting.lp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class LpRecipePriorityKey implements Comparable<LpRecipePriorityKey> {
    // Used for sorting recipes by priority in the crafting solver
    // Required in order to simulate the inherited priority you get from the traditional approach
    // For example when recipe A > recipe B, and so it tries A's child C before B even if B>C
    private final List<Integer> values;

    LpRecipePriorityKey() {
        this.values = List.of();
    }

    private LpRecipePriorityKey(final List<Integer> values) {
        this.values = List.copyOf(values);
    }

    LpRecipePriorityKey appendRecipePriority(final LpPatternRecipe recipe) {
        Objects.requireNonNull(recipe, "recipe cannot be null");
        final List<Integer> copy = new ArrayList<>(values);
        copy.add(recipe.basePriority());
        return new LpRecipePriorityKey(copy);
    }

    @Override
    public int compareTo(final LpRecipePriorityKey other) {
        final int sharedSize = Math.min(values.size(), other.values.size());
        for (int i = 0; i < sharedSize; i++) {
            final int left = values.get(i);
            final int right = other.values.get(i);
            if (left != right) {
                return Integer.compare(right, left);
            }
        }
        return Integer.compare(values.size(), other.values.size());
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof LpRecipePriorityKey other && values.equals(other.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }
}
