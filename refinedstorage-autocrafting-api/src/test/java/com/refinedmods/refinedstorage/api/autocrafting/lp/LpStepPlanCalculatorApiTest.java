package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageImpl;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.X;
import static org.assertj.core.api.Assertions.assertThat;

class LpStepPlanCalculatorApiTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LpStepPlanCalculatorApiTest.class);

    @Test
    void shouldReturnEmptyWhenNoPatternsAreProvided() {
        final Optional<LpStepPlan> result = LpStepPlanCalculator.calculateSteps(
            List.of(),
            LOGGER,
            new RootStorageImpl(),
            B,
            1,
            CancellationToken.NONE
        );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldPlanAdditionalAmountOnTopOfAlreadyStoredTargetResource() {
        final Pattern recipe = pattern().ingredient(A, 1).output(B, 1).build();
        final RootStorage storage = new RootStorageImpl();
        final StorageImpl source = new StorageImpl();
        source.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        source.insert(B, 2, Action.EXECUTE, Actor.EMPTY);
        storage.addSource(source);

        final Optional<LpStepPlan> result = LpStepPlanCalculator.calculateSteps(
            List.of(recipe),
            LOGGER,
            storage,
            B,
            1,
            CancellationToken.NONE
        );

        assertThat(result).isPresent();
        assertThat(result.get().steps()).hasSize(1);
        assertThat(result.get().steps().getFirst().iterations()).isEqualTo(1);
    }

    @Test
    void shouldDetectCycleThroughByproductDependencies() {
        final Pattern first = pattern().ingredient(A, 1).output(B, 1).byproduct(C, 1).build();
        final Pattern second = pattern().ingredient(C, 1).output(X, 1).build();
        final Pattern third = pattern().ingredient(X, 1).output(A, 1).build();

        final boolean hasCycles = LpStepPlanCalculator.hasRecipeCycles(List.of(
            step(first, 0, 1),
            step(second, 1, 1),
            step(third, 2, 1)
        ));

        assertThat(hasCycles).isTrue();
    }

    @Test
    void shouldReturnFalseForAcyclicTwoPatternDependency() {
        // Kills hasRecipeCycles line 132 (final return true mutation) and
        // containsCycle line 152 (return true after traversal mutation).
        final Pattern first = pattern().ingredient(A, 1).output(B, 1).build();
        final Pattern second = pattern().ingredient(B, 1).output(C, 1).build();

        final boolean hasCycles = LpStepPlanCalculator.hasRecipeCycles(
            List.of(step(first, 0, 1), step(second, 1, 1))
        );

        assertThat(hasCycles).isFalse();
    }

    @Test
    void shouldReturnFalseForSinglePatternWithNoSelfDependency() {
        // Kills containsCycle line 146 (visiting.add returns true immediately → cycle reported).
        // If the mutation says "returns true on first add to visiting", every single-step
        // plan would falsely report a cycle.
        final Pattern independent = pattern().ingredient(A, 1).output(X, 1).build();

        final boolean hasCycles = LpStepPlanCalculator.hasRecipeCycles(
            List.of(step(independent, 0, 1))
        );

        assertThat(hasCycles).isFalse();
    }

    @Test
    void shouldReturnTrueForDirectMutualDependency() {
        // Kills containsCycle line 147 (return false instead of true when cycle found) and
        // hasRecipeCycles line 119 negated conditional (returns false when patterns not empty).
        final Pattern first = pattern().ingredient(A, 1).output(B, 1).build();
        final Pattern second = pattern().ingredient(B, 1).output(A, 1).build();

        final boolean hasCycles = LpStepPlanCalculator.hasRecipeCycles(
            List.of(step(first, 0, 1), step(second, 1, 1))
        );

        assertThat(hasCycles).isTrue();
    }

    @Test
    void shouldReturnFalseDiamondDependencyWithSharedIntermediatePattern() {
        // Kills containsCycle line 142 (visited check negation).
        // Diamond: A→B, A→C, B→D, C→D (D consumed by root). No actual cycle.
        // Without the visited check, we might revisit D and falsely report a cycle.
        final Pattern makeB = pattern().ingredient(A, 1).output(B, 1).build();
        final Pattern makeC = pattern().ingredient(A, 1).output(C, 1).build();
        final Pattern makeDfromB = pattern().ingredient(B, 1).output(X, 1).build();
        final Pattern makeDfromC = pattern().ingredient(C, 1).output(X, 1).build();

        final boolean hasCycles = LpStepPlanCalculator.hasRecipeCycles(
            List.of(step(makeB, 0, 1), step(makeC, 1, 1), step(makeDfromB, 2, 1), step(makeDfromC, 3, 1))
        );

        assertThat(hasCycles).isFalse();
    }

    private static LpExecutionPlanStep step(final Pattern pattern, final int index, final long amount) {
        return new LpExecutionPlanStep(LpPatternRecipe.fromPattern(pattern, index), amount);
    }
}
