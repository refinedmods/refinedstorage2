package com.refinedmods.refinedstorage.api.autocrafting.lp;

import java.util.Objects;

public record LpExecutionPlanStep(LpPatternRecipe recipe, long iterations) {
    public LpExecutionPlanStep {
        Objects.requireNonNull(recipe, "recipe cannot be null");
        if (iterations <= 0) {
            throw new IllegalArgumentException("iterations must be larger than zero");
        }
    }
}
