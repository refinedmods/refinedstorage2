package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlan;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.X;
import static org.assertj.core.api.Assertions.assertThat;

class LpDispatcherHelperTest {
    @Test
    void shouldReturnEmptyTaskPlanWhenRootPatternIsMissing() {
        final Pattern pattern = pattern().ingredient(A, 1).output(B, 1).build();

        final Optional<TaskPlan> result = LpDispatcherHelper.toTaskPlan(
            C,
            1,
            List.of(step(pattern, 0, 1))
        );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldAggregateIterationsAndUseInternalProductionForInitialRequirements() {
        final Pattern produceB = pattern().ingredient(A, 1).output(B, 1).build();
        final Pattern produceC = pattern().ingredient(B, 1).output(C, 1).build();

        final Optional<TaskPlan> optionalPlan = LpDispatcherHelper.toTaskPlan(
            C,
            2,
            List.of(
                step(produceB, 0, 1),
                step(produceB, 0, 1),
                step(produceC, 1, 2)
            )
        );

        assertThat(optionalPlan).isPresent();
        final TaskPlan plan = optionalPlan.get();
        assertThat(plan.rootPattern()).isEqualTo(produceC);
        assertThat(plan.getPattern(produceB))
            .usingRecursiveComparison()
            .isEqualTo(new TaskPlan.PatternPlan(false, 2, java.util.Map.of(0, java.util.Map.of(A, 2L))));
        assertThat(plan.getPattern(produceC))
            .usingRecursiveComparison()
            .isEqualTo(new TaskPlan.PatternPlan(true, 2, java.util.Map.of(0, java.util.Map.of(B, 2L))));
        assertThat(plan.initialRequirements())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new com.refinedmods.refinedstorage.api.resource.ResourceAmount(A, 2));
    }

    @Test
    void shouldUseRequestedAmountOverrideInSingleStepPlan() {
        final Pattern pattern = pattern().ingredient(A, 1).output(B, 3).build();

        final TaskPlan plan = LpDispatcherHelper.toSingleStepPlan(
            C,
            5,
            step(pattern, 0, 2),
            true
        );

        assertThat(plan.resource()).isEqualTo(B);
        assertThat(plan.amount()).isEqualTo(5);
        assertThat(plan.initialRequirements())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new com.refinedmods.refinedstorage.api.resource.ResourceAmount(A, 2));
    }

    @Test
    void shouldFallbackToOutputAmountWhenRequestedAmountIsNotPositive() {
        final Pattern pattern = pattern().ingredient(A, 2).output(B, 4).build();

        final TaskPlan plan = LpDispatcherHelper.toSingleStepPlan(
            X,
            -1,
            step(pattern, 0, 3),
            false
        );

        assertThat(plan.resource()).isEqualTo(B);
        assertThat(plan.amount()).isEqualTo(12);
        assertThat(plan.getPattern(pattern))
            .usingRecursiveComparison()
            .isEqualTo(new TaskPlan.PatternPlan(false, 3, java.util.Map.of(0, java.util.Map.of(A, 6L))));
    }

    @Test
    void shouldFindLastMatchingRootPattern() {
        final Pattern first = pattern().ingredient(A, 1).output(C, 1).build();
        final Pattern second = pattern().ingredient(B, 1).output(C, 1).build();

        final Pattern root = LpDispatcherHelper.findRootPattern(
            C,
            List.of(step(first, 0, 1), step(second, 1, 1))
        );

        assertThat(root).isEqualTo(second);
    }

    @Test
    void shouldFindRootPatternWhenOnlyFirstStepInPlan() {
        // Kills findRootPattern / findRootPatternPrivate boundary mutation (>= 0 vs > 0).
        // With the boundary mutation the loop never visits index=0 and returns null.
        final Pattern only = pattern().ingredient(A, 1).output(B, 1).build();

        final Pattern found = LpDispatcherHelper.findRootPattern(B, List.of(step(only, 0, 1)));

        assertThat(found).isEqualTo(only);
    }

    @Test
    void shouldFindRootPatternSkippingLastStepThatDoesNotProduceTarget() {
        // Kills lambda$findRootPattern$6 "replaced boolean return with true" mutation.
        // If the anyMatch lambda always returns true, the last step (producing D, not C)
        // would be returned as root instead of the actual root.
        final Pattern producesC = pattern().ingredient(A, 1).output(C, 1).build();
        final Pattern producesD = pattern().ingredient(B, 1).output(X, 1).build();

        final Pattern found = LpDispatcherHelper.findRootPattern(
            C,
            List.of(step(producesC, 0, 1), step(producesD, 1, 1))
        );

        assertThat(found).isEqualTo(producesC);
    }

    @Test
    void shouldUseOutputAmountWhenRequestedAmountIsZero() {
        // Kills toSingleStepPlan conditional-boundary mutation (>= 0 instead of > 0).
        // With the mutation, requestedAmount=0 satisfies >=0 and taskAmount becomes 0.
        final Pattern pattern = pattern().ingredient(A, 1).output(B, 4).build();

        final TaskPlan plan = LpDispatcherHelper.toSingleStepPlan(X, 0, step(pattern, 0, 2), false);

        // taskAmount must be outputAmount = 4 * 2 = 8, not 0
        assertThat(plan.amount()).isEqualTo(8);
    }

    @Test
    void shouldCountByproductAsInternalProductionReducingExternalRequirement() {
        // Kills addIterationOutputs byproducts-forEach removal mutation.
        // step1 (non-root) produces main output B and byproduct C; step2 (root) needs C.
        // If byproducts are not added to internal storage, C appears in initialRequirements.
        final Pattern step1 = pattern().ingredient(A, 1).output(B, 1).byproduct(C, 1).build();
        final Pattern step2 = pattern().ingredient(C, 1).output(X, 1).build();

        final Optional<TaskPlan> optionalPlan = LpDispatcherHelper.toTaskPlan(
            X,
            1,
            List.of(step(step1, 0, 1), step(step2, 1, 1))
        );

        assertThat(optionalPlan).isPresent();
        // With correct byproduct tracking, C from step1's byproduct covers step2's need
        final var requirements = optionalPlan.get().initialRequirements();
        assertThat(requirements.stream().map(r -> r.resource()).toList()).doesNotContain(C);
        assertThat(requirements.stream().map(r -> r.resource()).toList()).containsOnly(A);
    }

    @Test
    void shouldDepletInternalStorageWhenItCoversDemandExactly() {
        // Kills lambda$consumeIterationInputs$3 subtraction→addition mutation (line 153).
        // step1 produces 1 X, step2 (non-root) uses 1 X, step3 (root) also needs 1 X.
        // With correct subtraction, X is depleted after step2 so step3 requires X externally.
        // With addition mutation, X stays in internal storage and step3 doesn't require X.
        final Pattern step1 = pattern().ingredient(A, 1).output(X, 1).build();
        final Pattern step2 = pattern().ingredient(X, 1).output(B, 1).build();
        final Pattern step3 = pattern().ingredient(X, 1).output(C, 1).build();

        final Optional<TaskPlan> optionalPlan = LpDispatcherHelper.toTaskPlan(
            C,
            1,
            List.of(step(step1, 0, 1), step(step2, 1, 1), step(step3, 2, 1))
        );

        assertThat(optionalPlan).isPresent();
        final var requirements = optionalPlan.get().initialRequirements();
        assertThat(requirements.stream().map(r -> r.resource()).toList()).contains(X);
    }

    @Test
    void shouldNotDeductFromInternalStorageWhenNoneAvailable() {
        // Kills lambda$consumeIterationInputs$3 negated-conditional mutation (line 152).
        // With negated fromInternalStorage>0 condition, it updates the map even when 0 was consumed,
        // which can corrupt other resources' storage entries.
        final Pattern step1 = pattern().ingredient(A, 2).output(X, 1).build();

        final Optional<TaskPlan> optionalPlan = LpDispatcherHelper.toTaskPlan(
            X,
            2,
            List.of(step(step1, 0, 2))
        );

        assertThat(optionalPlan).isPresent();
        // Both iterations need A from external (no internal source of A)
        final var requirements = optionalPlan.get().initialRequirements();
        assertThat(requirements)
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new com.refinedmods.refinedstorage.api.resource.ResourceAmount(A, 4));
    }

    private static LpExecutionPlanStep step(final Pattern pattern, final int index, final long amount) {
        return new LpExecutionPlanStep(LpPatternRecipe.fromPattern(pattern, index), amount);
    }
}
