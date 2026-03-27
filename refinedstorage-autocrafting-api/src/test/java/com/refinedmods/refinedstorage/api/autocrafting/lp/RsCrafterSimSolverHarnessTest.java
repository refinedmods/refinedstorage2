package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Java translations of all solver harness cases from the rs_crafter_sim Rust prototype
 * ({@code tests/cases/01_basics.json}, {@code 02_priorities.json},
 * {@code 03_cycles.json}, {@code 04_stress.json}).
 *
 * <p>Maps the Rust {@code find_executable_solution_via_cycle_elimination} to
 * {@link LpCraftingSolver#findExecutableSolutionViaCycleElimination}.
 * <ul>
 *   <li>Rust {@code Ok(solution, plan)} → {@code executableResult().isPresent()}</li>
 *   <li>Rust {@code Err(disabled_ids)} → {@code executableResult().isEmpty()}</li>
 * </ul>
 */
class RsCrafterSimSolverHarnessTest {
    private final LpCraftingSolver solver = new LpCraftingSolver();

    // -------------------------------------------------------------------------
    // Infrastructure helpers

    private static ResourceKey item(final int id) {
        return new ItemId(id);
    }

    /**
     * Builds an {@link LpPatternRecipe} from flat int arrays.
     * {@code inputs}  – each element is {itemId, amount}
     * {@code outputs} – each element is {itemId, amount}
     */
    private static LpPatternRecipe recipe(final int[][] inputs,
                                          final int[][] outputs,
                                          final int priority) {
        var builder = pattern();
        for (final int[] input : inputs) {
            builder = builder.ingredient(item(input[0]), input[1]);
        }
        for (final int[] output : outputs) {
            builder = builder.output(item(output[0]), output[1]);
        }
        return LpPatternRecipe.fromPattern(builder.build(), priority);
    }

    /**
     * Builds an {@link LpResourceSet} from flat (itemId, amount) pairs.
     * Zero amounts are skipped (equivalent to the Rust ItemSet behaviour).
     */
    private static LpResourceSet resources(final int... idAmountFlat) {
        final LpResourceSet set = new LpResourceSet();
        for (int i = 0; i < idAmountFlat.length; i += 2) {
            if (idAmountFlat[i + 1] > 0) {
                set.addAmount(item(idAmountFlat[i]), idAmountFlat[i + 1]);
            }
        }
        return set;
    }

    /**
     * Runs the cycle-elimination solver and asserts the plan is executable.
     * Mirrors the inner "ok" branch of the Rust harness for each case.
     */
    private void assertOk(final List<LpPatternRecipe> recipes,
                          final LpResourceSet startingItems,
                          final LpResourceSet target,
                          final long[] expectedInvocations,
                          final int[][] expectedRemainingInventory) {
        final LpCraftingSolver.CycleEliminationResult result =
            solver.findExecutableSolutionViaCycleElimination(recipes, startingItems, target);

        assertThat(result.executableResult())
            .as("expected an executable plan")
            .isPresent();

        final LpCraftingSolution solution = result.executableResult().get().solution();

        for (int i = 0; i < expectedInvocations.length; i++) {
            assertThat(solution.recipeUsageCount(recipes.get(i)))
                .as("invocation count for recipe[%d]", i)
                .isEqualTo(expectedInvocations[i]);
        }

        for (final int[] itemAndCount : expectedRemainingInventory) {
            assertThat(solution.finalInventoryCount(item(itemAndCount[0])))
                .as("final inventory of item %d", itemAndCount[0])
                .isEqualTo(itemAndCount[1]);
        }
    }

    /** Asserts that cycle elimination can find no executable plan. */
    private void assertError(final List<LpPatternRecipe> recipes,
                             final LpResourceSet startingItems,
                             final LpResourceSet target) {
        final LpCraftingSolver.CycleEliminationResult result =
            solver.findExecutableSolutionViaCycleElimination(recipes, startingItems, target);

        assertThat(result.executableResult())
            .as("expected no executable plan (error)")
            .isEmpty();
    }

    // =========================================================================
    // 01_basics.json
    // =========================================================================

    @Test
    void basics_twoStepChainSuccess() {
        // Tests that a simple two-step crafting chain executes successfully, converting input resources through intermediate items to reach the target.
        // Inputs: 2x item0. Recipes: item0→item1, item1→item2. Target: 2x item2.
        // Each recipe runs twice; final inventory item0=0, item1=0, item2=2.
        final List<LpPatternRecipe> recipes = List.of(
            recipe(new int[][]{{0, 1}}, new int[][]{{1, 1}}, 0),
            recipe(new int[][]{{1, 1}}, new int[][]{{2, 1}}, 0)
        );
        assertOk(
            recipes,
            resources(0, 2),
            resources(2, 2),
            new long[]{2, 2},
            new int[][]{{0, 0}, {1, 0}, {2, 2}}
        );
    }

    @Test
    void basics_infeasibleTargetProducesError() {
        // Tests that the solver correctly fails when the target item cannot be produced by any available recipe.
        // Inputs: 1x item0. Only recipe: item0→item1. Target: 1x item2.
        // No recipe produces item2 → infeasible.
        final List<LpPatternRecipe> recipes = List.of(
            recipe(new int[][]{{0, 1}}, new int[][]{{1, 1}}, 0)
        );
        assertError(recipes, resources(0, 1), resources(2, 1));
    }

    // =========================================================================
    // 02_priorities.json
    // =========================================================================

    @Test
    void priorities_higherPriorityValueRouteIsSelected() {
        // Tests that when multiple recipes produce the same output, only the highest priority recipe is used in the solution.
        // Two alternative item0→item2 recipes with priorities 0 and 10.
        // Only the higher-priority one (index 1) should run.
        final List<LpPatternRecipe> recipes = List.of(
            recipe(new int[][]{{0, 1}}, new int[][]{{2, 1}}, 0),
            recipe(new int[][]{{0, 1}}, new int[][]{{2, 1}}, 10)
        );
        assertOk(
            recipes,
            resources(0, 1),
            resources(2, 1),
            new long[]{0, 1},
            new int[][]{{0, 0}, {2, 1}}
        );
    }

    @Test
    void priorities_unrelatedRecipeIsEliminated() {
        // Tests that recipes unrelated to the crafting goal are pruned and not used, even if they have high priority.
        // item0→item2 (relevant) and item3→item4 (irrelevant). Target: 1x item2.
        // Relevance pruning drops the irrelevant recipe; its usage stays 0.
        final List<LpPatternRecipe> recipes = List.of(
            recipe(new int[][]{{0, 1}}, new int[][]{{2, 1}}, 0),
            recipe(new int[][]{{3, 1}}, new int[][]{{4, 1}}, 999)
        );
        assertOk(
            recipes,
            resources(0, 1, 3, 10),
            resources(2, 1),
            new long[]{1, 0},
            new int[][]{{0, 0}, {2, 1}}
        );
    }

    // =========================================================================
    // 03_cycles.json
    // =========================================================================

    @Test
    void cycles_singleItemSelfCycleExecutesWithSeed() {
        // Tests that a self-amplifying recipe can execute when there is enough seed material to start the cycle.
        // item0 →2x item0 (self-amplifying). Have 1x item0; need 2x item0.
        // One execution: consume 1, produce 2 → final item0 = 2.
        final List<LpPatternRecipe> recipes = List.of(
            recipe(new int[][]{{0, 1}}, new int[][]{{0, 2}}, 0)
        );
        assertOk(
            recipes,
            resources(0, 1),
            resources(0, 2),
            new long[]{1},
            new int[][]{{0, 2}}
        );
    }

    @Test
    void cycles_singleItemSelfCycleWithoutSeedErrors() {
        // Tests that a self-amplifying cycle fails when there is no starting material to bootstrap the cycle.
        // Same self-amplifying recipe but starting with 0x item0 → cannot start.
        final List<LpPatternRecipe> recipes = List.of(
            recipe(new int[][]{{0, 1}}, new int[][]{{0, 2}}, 0)
        );
        assertError(recipes, resources(0, 0), resources(0, 1));
    }

    @Test
    void cycles_largerCycleWithoutSeedErrors() {
        // Tests that a multi-recipe cycle fails when there is no seed material to initiate the cycle.
        // 4-recipe cycle 0→1→2→3→0 (amplifying return). No starting resources.
        // No seed → execution impossible.
        final List<LpPatternRecipe> recipes = List.of(
            recipe(new int[][]{{0, 1}}, new int[][]{{1, 1}}, 0),
            recipe(new int[][]{{1, 1}}, new int[][]{{2, 1}}, 0),
            recipe(new int[][]{{2, 1}}, new int[][]{{3, 1}}, 0),
            recipe(new int[][]{{3, 1}}, new int[][]{{0, 2}}, 0)
        );
        assertError(recipes, resources(0, 0), resources(0, 1));
    }

    @Test
    void cycles_overlappingCyclesWithoutSeedError() {
        // Tests that overlapping cycles without seed material fail execution.
        // Two cycles sharing item1: (item0↔item1) and (item1↔item2). No seed.
        final List<LpPatternRecipe> recipes = List.of(
            recipe(new int[][]{{0, 1}}, new int[][]{{1, 1}}, 0),
            recipe(new int[][]{{1, 1}}, new int[][]{{0, 2}}, 0),
            recipe(new int[][]{{1, 1}}, new int[][]{{2, 1}}, 0),
            recipe(new int[][]{{2, 1}}, new int[][]{{1, 2}}, 0)
        );
        assertError(recipes, resources(0, 0, 1, 0, 2, 0), resources(0, 1));
    }

    @Test
    void cycles_cycleThatIncludesTargetItemErrors() {
        // Tests that a cycle containing the target item fails when there is no seed to bootstrap it.
        // Cycle item0↔item1. Target is item0 (inside the cycle). No seed.
        final List<LpPatternRecipe> recipes = List.of(
            recipe(new int[][]{{0, 1}}, new int[][]{{1, 1}}, 0),
            recipe(new int[][]{{1, 1}}, new int[][]{{0, 2}}, 0)
        );
        assertError(recipes, resources(0, 0, 1, 0), resources(0, 1));
    }

    @Test
    void cycles_negativeCycleErrors() {
        // Tests that a net-negative cycle (consumes more than it produces) fails even with sufficient starting material.
        // Recipe: 2x item0 → 1x item0 (net −1 per run). Have 10x item0; need 11.
        // Running the recipe only decreases item0 → impossible to reach 11.
        final List<LpPatternRecipe> recipes = List.of(
            recipe(new int[][]{{0, 2}}, new int[][]{{0, 1}}, 0)
        );
        assertError(recipes, resources(0, 10), resources(0, 11));
    }

    // =========================================================================
    // 04_stress.json
    // =========================================================================

    /**
     * 20-item exponential chain: item_i+1 requires 2x item_i (i=0..18), then
     * recipe[19] converts 1x item19 into 1&nbsp;048&nbsp;577x item0.
     * Starting with 524&nbsp;288x item0.
     */
    @Test
    void stress_twentyItemExponentialChainWithTerminalCycleRecipe() {
        // Tests solver performance on a complex exponential chain with a terminal cycle recipe for system stress testing.
        final List<LpPatternRecipe> recipes = new ArrayList<>();
        for (int i = 0; i < 19; i++) {
            recipes.add(recipe(new int[][]{{i, 2}}, new int[][]{{i + 1, 1}}, 0));
        }
        recipes.add(recipe(new int[][]{{19, 1}}, new int[][]{{0, 1_048_577}}, 0));

        assertOk(
            recipes,
            resources(0, 524_288),
            resources(0, 1_048_577),
            new long[]{262144, 131072, 65536, 32768, 16384, 8192, 4096, 2048,
                1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 1},
            new int[][]{{0, 1_048_577}, {19, 0}}
        );
    }

    /**
     * Scaled-down version of the 04_stress chain: 5 items with powers-of-2 scaling
     * ({@code item0 x2→item1, item1 x2→item2, ..., item4 x1→item0 x33}).
     * Starting with 16x item0; target 33x item0.
     * Expected invocations: [8, 4, 2, 1, 1]; final item0 = 33.
     */
    @Test
    void stress_smallExponentialChain_fiveItem() {
        // Tests a scaled-down exponential chain scenario to verify correct recipe invocation counts and final inventory values.
        final List<LpPatternRecipe> recipes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            recipes.add(recipe(new int[][]{{i, 2}}, new int[][]{{i + 1, 1}}, 0));
        }
        recipes.add(recipe(new int[][]{{4, 1}}, new int[][]{{0, 33}}, 0));

        assertOk(
            recipes,
            resources(0, 16),
            resources(0, 33),
            new long[]{8, 4, 2, 1, 1},
            new int[][]{{0, 33}, {4, 0}}
        );
    }

    /** Lightweight integer-keyed resource, equivalent to Rust's item-ID usize. */
    private record ItemId(int id) implements ResourceKey {
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof ItemId other && id == other.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
