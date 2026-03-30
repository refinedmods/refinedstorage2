package com.refinedmods.refinedstorage.api.network.impl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpExecutionPlanStep;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpPatternRecipe;

import java.util.List;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.D;
import static org.assertj.core.api.Assertions.assertThat;

class LpStepPlanCalculatorTest {
    @Test
    void shouldNotReportCyclesWhenNoStepsExist() {
        assertThat(LpStepPlanCalculator.hasRecipeCycles(List.of())).isFalse();
    }

    @Test
    void shouldNotReportCyclesForAcyclicDependencies() {
        final Pattern first = pattern().ingredient(A, 1).output(B, 1).build();
        final Pattern second = pattern().ingredient(B, 1).output(C, 1).build();
        final Pattern third = pattern().ingredient(C, 1).output(D, 1).build();

        final List<LpExecutionPlanStep> steps = List.of(
            step(first, 0, 1),
            step(second, 1, 1),
            step(third, 2, 1)
        );

        assertThat(LpStepPlanCalculator.hasRecipeCycles(steps)).isFalse();
    }

    @Test
    void shouldReportCyclesForMutuallyDependentPatterns() {
        final Pattern first = pattern().ingredient(B, 1).output(A, 1).build();
        final Pattern second = pattern().ingredient(A, 1).output(B, 1).build();

        final List<LpExecutionPlanStep> steps = List.of(
            step(first, 0, 1),
            step(second, 1, 1)
        );

        assertThat(LpStepPlanCalculator.hasRecipeCycles(steps)).isTrue();
    }

    @Test
    void shouldReportCyclesForSelfDependentPattern() {
        final Pattern selfDependent = pattern().ingredient(A, 1).output(A, 1).build();

        final List<LpExecutionPlanStep> steps = List.of(step(selfDependent, 0, 1));

        assertThat(LpStepPlanCalculator.hasRecipeCycles(steps)).isTrue();
    }

    private static LpExecutionPlanStep step(final Pattern pattern, final int index, final long amount) {
        return new LpExecutionPlanStep(LpPatternRecipe.fromPattern(pattern, index), amount);
    }
}