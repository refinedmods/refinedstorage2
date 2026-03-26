package com.refinedmods.refinedstorage.api.autocrafting.lp;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LpCraftingSolverValidationTest {
    @Test
    void constructorShouldValidateOptions() {
        // Tests that the solver constructor validates and rejects null options.
        assertThatThrownBy(() -> new LpCraftingSolver(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("options cannot be null");
    }

    @Test
    void publicApisShouldValidateNullArguments() {
        // Tests that all public solver methods validate their arguments and reject null values.
        final LpCraftingSolver solver = new LpCraftingSolver();
        final List<LpPatternRecipe> recipes = List.of(recipe());
        final LpResourceSet resources = new LpResourceSet();

        assertThatThrownBy(() -> solver.solve(null, resources, resources))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> solver.solve(recipes, null, resources))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> solver.solve(recipes, resources, null))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> solver.computeRequiredBaseItems(null, resources, resources))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("recipes cannot be null");
        assertThatThrownBy(() -> solver.findExecutableSolutionViaCycleElimination(null, resources, resources))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("recipes cannot be null");
    }

    @Test
    void executablePlanResultRecordShouldValidateAndCopy() {
        // Tests that ExecutablePlanResult validates inputs and creates defensive copies of mutable collections.
        final LpPatternRecipe recipe = recipe();
        final LpCraftingSolution solution = new LpCraftingSolution(Map.of(), Map.of(), List.of(A));
        final List<LpExecutionPlanStep> mutablePlan =
            new java.util.ArrayList<>(List.of(new LpExecutionPlanStep(recipe, 1)));

        final LpCraftingSolver.ExecutablePlanResult result =
            new LpCraftingSolver.ExecutablePlanResult(solution, mutablePlan);
        mutablePlan.clear();

        assertThat(result.plan()).hasSize(1);

        assertThatThrownBy(() -> new LpCraftingSolver.ExecutablePlanResult(null, List.of()))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("solution cannot be null");
        assertThatThrownBy(() -> new LpCraftingSolver.ExecutablePlanResult(solution, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void cycleEliminationAndPlanningOutcomeRecordsShouldValidateAndCopy() {
        // Tests that result record types validate inputs and create defensive copies of disabled recipe IDs.
        final LpCraftingSolver.CycleEliminationResult cycle = new LpCraftingSolver.CycleEliminationResult(
            Optional.empty(),
            Set.of(UUID.randomUUID())
        );
        assertThat(cycle.fallbackDisabledRecipeIds()).hasSize(1);

        assertThatThrownBy(() -> new LpCraftingSolver.CycleEliminationResult(null, Set.of()))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("executableResult cannot be null");
        assertThatThrownBy(() -> new LpCraftingSolver.CycleEliminationResult(Optional.empty(), null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("fallbackDisabledRecipeIds cannot be null");

        final java.util.Set<UUID> mutable = new java.util.LinkedHashSet<>();
        mutable.add(UUID.randomUUID());
        final LpCraftingSolver.PlanningOutcome outcome = new LpCraftingSolver.PlanningOutcome(
            1,
            Optional.empty(),
            new LpResourceSet(),
            mutable
        );
        mutable.clear();

        assertThat(outcome.fallbackDisabledRecipeIds()).hasSize(1);

        assertThatThrownBy(() -> new LpCraftingSolver.PlanningOutcome(1, null, new LpResourceSet(), Set.of()))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("executableResult cannot be null");
        assertThatThrownBy(() -> new LpCraftingSolver.PlanningOutcome(1, Optional.empty(), null, Set.of()))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("requiredBaseItems cannot be null");
        assertThatThrownBy(() -> new LpCraftingSolver.PlanningOutcome(1, Optional.empty(), new LpResourceSet(), null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("fallbackDisabledRecipeIds cannot be null");
    }

    private static LpPatternRecipe recipe() {
        return LpPatternRecipe.fromPattern(pattern().ingredient(A, 1).output(B, 1).build(), 0);
    }
}
