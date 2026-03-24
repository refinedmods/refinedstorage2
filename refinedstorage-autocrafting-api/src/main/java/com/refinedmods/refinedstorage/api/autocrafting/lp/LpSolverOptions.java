package com.refinedmods.refinedstorage.api.autocrafting.lp;

/**
 * Bounds for the standalone pure-Java prototype implementation.
 */
public record LpSolverOptions(int recipeUpperBound, int maxSearchNodes, int maxCycleEliminationBranches) {
    public static final int DEFAULT_RECIPE_UPPER_BOUND = 128;
    public static final int DEFAULT_MAX_SEARCH_NODES = 250_000;
    public static final int DEFAULT_MAX_CYCLE_ELIMINATION_BRANCHES = 512;

    public LpSolverOptions {
        if (recipeUpperBound <= 0) {
            throw new IllegalArgumentException("recipeUpperBound must be larger than zero");
        }
        if (maxSearchNodes <= 0) {
            throw new IllegalArgumentException("maxSearchNodes must be larger than zero");
        }
        if (maxCycleEliminationBranches <= 0) {
            throw new IllegalArgumentException("maxCycleEliminationBranches must be larger than zero");
        }
    }

    public static LpSolverOptions defaults() {
        return new LpSolverOptions(
            DEFAULT_RECIPE_UPPER_BOUND,
            DEFAULT_MAX_SEARCH_NODES,
            DEFAULT_MAX_CYCLE_ELIMINATION_BRANCHES
        );
    }
}
