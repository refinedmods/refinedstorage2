package com.refinedmods.refinedstorage.api.network.impl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpExecutionPlanStep;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpStepPlan;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpStepPlanCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpPatternRecipe;
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
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.D;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LpStepPlanCalculatorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LpStepPlanCalculatorTest.class);

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

    @Test
    void shouldReportCyclesWhenDependencyIsProducedAsByproduct() {
        final Pattern first = pattern().ingredient(C, 1).output(A, 1).byproduct(B, 1).build();
        final Pattern second = pattern().ingredient(B, 1).output(C, 1).build();

        final List<LpExecutionPlanStep> steps = List.of(
            step(first, 0, 1),
            step(second, 1, 1)
        );

        assertThat(LpStepPlanCalculator.hasRecipeCycles(steps)).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenCalculationIsCancelled() {
        final Optional<LpStepPlan> result = LpStepPlanCalculator.calculateSteps(
            List.of(pattern().ingredient(A, 1).output(B, 1).build()),
            LOGGER,
            new RootStorageImpl(),
            B,
            1,
            new CancellationToken() {
                @Override
                public boolean isCancelled() {
                    return true;
                }

                @Override
                public void cancel() {
                }
            }
        );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFailFastWhenLpIncompatiblePatternIsProvided() {
        final Pattern fuzzy = pattern().ingredient(1).input(A).input(B).end().output(C, 1).build();

        assertThatThrownBy(() -> LpStepPlanCalculator.calculateSteps(
            List.of(fuzzy),
            LOGGER,
            new RootStorageImpl(),
            C,
            1,
            CancellationToken.NONE
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("LP solver received LP-incompatible pattern");
    }

    @Test
    void shouldFailFastWhenAnyLpIncompatiblePatternIsProvided() {
        final Pattern compatible = pattern().ingredient(A, 1).output(B, 1).build();
        final Pattern fuzzy = pattern().ingredient(1).input(A).input(B).end().output(C, 1).build();

        assertThatThrownBy(() -> LpStepPlanCalculator.calculateSteps(
            List.of(compatible, fuzzy),
            LOGGER,
            new RootStorageImpl(),
            B,
            1,
            CancellationToken.NONE
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("LP solver received LP-incompatible pattern");
    }

    @Test
    void shouldReturnEmptyWhenNoExecutablePlanExists() {
        final Pattern recipe = pattern().ingredient(A, 1).output(B, 1).build();

        final Optional<LpStepPlan> result = LpStepPlanCalculator.calculateSteps(
            List.of(recipe),
            LOGGER,
            new RootStorageImpl(),
            B,
            1,
            CancellationToken.NONE
        );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnPlanWhenExecutablePlanExists() {
        final Pattern recipe = pattern().ingredient(A, 1).output(B, 1).build();
        final RootStorage rootStorage = new RootStorageImpl();
        final StorageImpl source = new StorageImpl();
        source.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        rootStorage.addSource(source);

        final Optional<LpStepPlan> result = LpStepPlanCalculator.calculateSteps(
            List.of(recipe),
            LOGGER,
            rootStorage,
            B,
            1,
            CancellationToken.NONE
        );

        assertThat(result).isPresent();
        assertThat(result.get().steps()).isNotEmpty();
    }

    private static LpExecutionPlanStep step(final Pattern pattern, final int index, final long amount) {
        return new LpExecutionPlanStep(LpPatternRecipe.fromPattern(pattern, index), amount);
    }
}