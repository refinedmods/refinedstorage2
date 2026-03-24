package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures;

import java.util.List;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static org.assertj.core.api.Assertions.assertThat;

class LpCraftingSolverAlgorithmSelectionTest {
    private final LpCraftingSolver solver = new LpCraftingSolver();

    @Test
    void shouldUseExecutablePlanPathWhenResourcesPermitLinearCraft() {
        final List<LpPatternRecipe> recipes = List.of(
            LpPatternRecipe.fromPattern(
                pattern().ingredient(ResourceFixtures.A, 1).output(ResourceFixtures.B, 1).build(),
                0
            )
        );
        final LpResourceSet startingResources = resourceSet(ResourceFixtures.A, 4);
        final LpResourceSet target = resourceSet(ResourceFixtures.B, 2);

        final LpCraftingSolver.PlanningOutcome outcome = solver.solve(recipes, startingResources, target);

        assertThat(classify(outcome)).isEqualTo(PlanningAlgorithm.EXECUTABLE_PLAN);
        assertThat(outcome.executableResult()).isPresent();
    }

    @Test
    void shouldUseBaseItemsPathWhenNothingIsCraftable() {
        final List<LpPatternRecipe> recipes = List.of();
        final LpResourceSet startingResources = new LpResourceSet();
        final LpResourceSet target = resourceSet(ResourceFixtures.B, 3);

        final LpCraftingSolver.PlanningOutcome outcome = solver.solve(recipes, startingResources, target);

        assertThat(classify(outcome)).isEqualTo(PlanningAlgorithm.BASE_ITEMS_ONLY);
        assertThat(outcome.requiredBaseItems().getAmount(ResourceFixtures.B)).isEqualTo(3);
    }

    @Test
    void shouldUseCycleFallbackPathWhenLpSolutionIsNotExecutableInOrder() {
        final List<LpPatternRecipe> recipes = List.of(
            LpPatternRecipe.fromPattern(
                pattern().ingredient(ResourceFixtures.A, 1).output(ResourceFixtures.B, 1).build(),
                0
            ),
            LpPatternRecipe.fromPattern(
                pattern()
                    .ingredient(ResourceFixtures.B, 1)
                    .output(ResourceFixtures.A, 1)
                    .output(ResourceFixtures.B, 1)
                    .build(),
                1
            )
        );
        final LpResourceSet startingResources = new LpResourceSet();
        final LpResourceSet target = resourceSet(ResourceFixtures.B, 1);

        final LpCraftingSolver.PlanningOutcome outcome = solver.solve(recipes, startingResources, target);

        assertThat(classify(outcome)).isEqualTo(PlanningAlgorithm.CYCLE_FALLBACK);
        assertThat(outcome.fallbackDisabledRecipeIds()).isNotEmpty();
        assertThat(outcome.executableResult()).isEmpty();
    }

    private static LpResourceSet resourceSet(final ResourceFixtures resource, final long amount) {
        final LpResourceSet resourceSet = new LpResourceSet();
        resourceSet.setAmount(resource, amount);
        return resourceSet;
    }

    private static PlanningAlgorithm classify(final LpCraftingSolver.PlanningOutcome outcome) {
        if (outcome.executableResult().isPresent()) {
            return PlanningAlgorithm.EXECUTABLE_PLAN;
        }
        if (outcome.maxCraftableAmount() == 0) {
            return PlanningAlgorithm.BASE_ITEMS_ONLY;
        }
        return PlanningAlgorithm.CYCLE_FALLBACK;
    }

    private enum PlanningAlgorithm {
        EXECUTABLE_PLAN,
        BASE_ITEMS_ONLY,
        CYCLE_FALLBACK
    }
}
