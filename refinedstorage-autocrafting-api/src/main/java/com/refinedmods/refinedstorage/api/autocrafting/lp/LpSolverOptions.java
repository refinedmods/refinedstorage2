package com.refinedmods.refinedstorage.api.autocrafting.lp;

public record LpSolverOptions(int recipeUpperBound, int maxCycleEliminationBranches) {
    // Bounds the LP solver's search space by upper-bounding
    // the number of times any given recipe can be used in a solution
    // and the number of cycle combinations that will be explored during cycle elimination.
    public static final int DEFAULT_RECIPE_UPPER_BOUND = 1_000_000_000;
    public static final int DEFAULT_MAX_CYCLE_ELIMINATION_BRANCHES = 512;

    public LpSolverOptions {
        if (recipeUpperBound <= 0) {
            throw new IllegalArgumentException("recipeUpperBound must be larger than zero");
        }
        if (maxCycleEliminationBranches <= 0) {
            throw new IllegalArgumentException("maxCycleEliminationBranches must be larger than zero");
        }
    }

    public static LpSolverOptions defaults() {
        return new LpSolverOptions(
            DEFAULT_RECIPE_UPPER_BOUND,
            DEFAULT_MAX_CYCLE_ELIMINATION_BRANCHES
        );
    }
}
