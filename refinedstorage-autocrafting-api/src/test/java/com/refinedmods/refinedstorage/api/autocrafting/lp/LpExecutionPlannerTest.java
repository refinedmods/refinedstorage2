package com.refinedmods.refinedstorage.api.autocrafting.lp;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.C;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LpExecutionPlannerTest {
    @Test
    void shouldValidateNullInputs() {
        final List<LpPatternRecipe> recipes = List.of(recipe(A, B, 1, 1, 0));
        final Map<java.util.UUID, Long> values = Map.of(recipes.getFirst().uniqueId(), 1L);
        final LpResourceSet resources = new LpResourceSet();

        assertThatThrownBy(() ->
            LpExecutionPlanner.buildExecutablePlanFromRecipeUsage(null, values, resources)
        )
            .isInstanceOf(NullPointerException.class)
            .hasMessage("recipes cannot be null");
        assertThatThrownBy(() ->
            LpExecutionPlanner.buildExecutablePlanFromRecipeUsage(recipes, null, resources)
        )
            .isInstanceOf(NullPointerException.class)
            .hasMessage("recipeValues cannot be null");
        assertThatThrownBy(() ->
            LpExecutionPlanner.buildExecutablePlanFromRecipeUsage(recipes, values, null)
        )
            .isInstanceOf(NullPointerException.class)
            .hasMessage("startingResources cannot be null");
    }

    @Test
    void shouldRejectNegativeRecipeUsage() {
        final LpPatternRecipe recipe = recipe(A, B, 1, 1, 0);

        assertThatThrownBy(() -> LpExecutionPlanner.buildExecutablePlanFromRecipeUsage(
            List.of(recipe),
            Map.of(recipe.uniqueId(), -1L),
            new LpResourceSet()
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Negative usage count");
    }

    @Test
    void shouldReturnEmptyPlanWhenNoRecipeUsageIsRequested() {
        final LpPatternRecipe recipe = recipe(A, B, 1, 1, 0);

        final java.util.Optional<List<LpExecutionPlanStep>> plan =
            LpExecutionPlanner.buildExecutablePlanFromRecipeUsage(
            List.of(recipe),
            Map.of(),
            new LpResourceSet()
        );

        assertThat(plan).isPresent();
        assertThat(plan.get()).isEmpty();
    }

    @Test
    void shouldBuildSimpleExecutablePlan() {
        final LpPatternRecipe recipe = recipe(A, B, 1, 1, 0);
        final LpResourceSet starting = new LpResourceSet();
        starting.setAmount(A, 3);

        final java.util.Optional<List<LpExecutionPlanStep>> plan =
            LpExecutionPlanner.buildExecutablePlanFromRecipeUsage(
            List.of(recipe),
            Map.of(recipe.uniqueId(), 3L),
            starting
        );

        assertThat(plan).isPresent();
        assertThat(plan.get()).hasSize(1);
        assertThat(plan.get().getFirst().recipe().uniqueId()).isEqualTo(recipe.uniqueId());
        assertThat(plan.get().getFirst().iterations()).isEqualTo(3);
    }

    @Test
    void shouldReturnEmptyWhenUsageCannotBeAfforded() {
        final LpPatternRecipe recipe = recipe(A, B, 2, 1, 0);
        final LpResourceSet starting = new LpResourceSet();
        starting.setAmount(A, 1);

        final java.util.Optional<List<LpExecutionPlanStep>> plan =
            LpExecutionPlanner.buildExecutablePlanFromRecipeUsage(
            List.of(recipe),
            Map.of(recipe.uniqueId(), 1L),
            starting
        );

        assertThat(plan).isEmpty();
    }

    @Test
    void shouldBacktrackAndRollbackWhenLaterRequirementIsImpossible() {
        final LpPatternRecipe neutral = recipe(A, A, 1, 1, 0);
        final LpPatternRecipe impossible = recipe(B, B, 1, 1, 0);

        final LpResourceSet starting = new LpResourceSet();
        starting.setAmount(A, 1);

        final java.util.Optional<List<LpExecutionPlanStep>> plan =
            LpExecutionPlanner.buildExecutablePlanFromRecipeUsage(
            List.of(neutral, impossible),
            Map.of(neutral.uniqueId(), 2L, impossible.uniqueId(), 1L),
            starting
        );

        assertThat(plan).isEmpty();
    }

    @Test
    void shouldHandleLoopAndNonLoopCandidateSorting() {
        final LpPatternRecipe loopAtoB = recipe(A, B, 1, 1, 0);
        final LpPatternRecipe loopBtoA = recipe(B, A, 1, 1, 0);
        final LpPatternRecipe nonLoopAtoC = recipe(A, C, 1, 1, 0);

        final LpResourceSet starting = new LpResourceSet();
        starting.setAmount(A, 2);
        starting.setAmount(B, 1);

        final java.util.Optional<List<LpExecutionPlanStep>> plan =
            LpExecutionPlanner.buildExecutablePlanFromRecipeUsage(
            List.of(loopAtoB, loopBtoA, nonLoopAtoC),
            Map.of(loopAtoB.uniqueId(), 1L, loopBtoA.uniqueId(), 1L, nonLoopAtoC.uniqueId(), 1L),
            starting
        );

        assertThat(plan).isPresent();
        assertThat(plan.get()).isNotEmpty();
    }

    private static LpPatternRecipe recipe(final com.refinedmods.refinedstorage.api.resource.ResourceKey input,
                                          final com.refinedmods.refinedstorage.api.resource.ResourceKey output,
                                          final long inputAmount,
                                          final long outputAmount,
                                          final int priority) {
        return LpPatternRecipe.fromPattern(
            pattern().ingredient(input, inputAmount).output(output, outputAmount).build(),
            priority
        );
    }
}
