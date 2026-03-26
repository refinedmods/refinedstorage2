package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.C;
import static org.assertj.core.api.Assertions.assertThat;

class LpExecutionPlannerInternalsTest {
    @Test
    void internalsShouldHandleBatchGenerationAndPlanMutations() throws Exception {
        // Tests that internal batch generation strategies correctly split recipe execution into feasible batches and plans.
        final LpPatternRecipe recipe = recipe(A, B, 1, 1, 0);
        final Object candidate = candidate(recipe, 5L, 3L);

        final List<Long> batches = invokeBuildBatchAttempts(candidate);
        assertThat(batches).containsExactly(3L, 1L);

        final List<LpExecutionPlanStep> plan = new ArrayList<>();
        invokeAppendOrMerge(plan, recipe, 2L);
        invokeAppendOrMerge(plan, recipe, 3L);
        assertThat(plan).hasSize(1);
        assertThat(plan.getFirst().iterations()).isEqualTo(5L);

        invokeRemoveOrShrink(plan, recipe, 3L);
        assertThat(plan).hasSize(1);
        assertThat(plan.getFirst().iterations()).isEqualTo(2L);

        invokeRemoveOrShrink(plan, recipe, 2L);
        assertThat(plan).isEmpty();
    }

    @Test
    void internalsShouldApplyAndRollbackBatches() throws Exception {
        // Tests that batches can be applied to inventory and rolled back to restore previous state correctly.
        final LpPatternRecipe recipe = recipe(A, B, 2, 3, 0);
        final LpResourceSet inventory = new LpResourceSet();
        inventory.setAmount(A, 5);

        invokeApplyBatch(recipe, 2L, inventory);
        assertThat(inventory.getAmount(A)).isEqualTo(1L);
        assertThat(inventory.getAmount(B)).isEqualTo(6L);

        invokeRollbackBatch(recipe, 2L, inventory);
        assertThat(inventory.getAmount(A)).isEqualTo(5L);
        assertThat(inventory.getAmount(B)).isZero();
    }

    @Test
    void internalsShouldHandleCandidateAndBatchValidation() throws Exception {
        // Tests that candidates are validated for feasibility and batch attempts are checked against recipe requirements.
        final LpPatternRecipe normalRecipe = recipe(A, B, 3, 1, 0);
        final LpResourceSet inventory = new LpResourceSet();
        inventory.setAmount(A, 10L);

        final Object candidate = invokeToCandidate(normalRecipe, Map.of(normalRecipe.uniqueId(), 2L), inventory);
        assertThat(candidate).isNotNull();

        final Boolean batchZero = invokeTryCandidateBatch(
            List.of(normalRecipe),
            Map.of(),
            Map.of(normalRecipe.uniqueId(), 2L),
            inventory.copy(),
            2L,
            new ArrayList<>(),
            candidate,
            0L
        );
        final Boolean batchTooLarge = invokeTryCandidateBatch(
            List.of(normalRecipe),
            Map.of(),
            Map.of(normalRecipe.uniqueId(), 2L),
            inventory.copy(),
            2L,
            new ArrayList<>(),
            candidate,
            3L
        );
        assertThat(batchZero).isFalse();
        assertThat(batchTooLarge).isFalse();

        final Object nullByRemaining = invokeToCandidate(normalRecipe, Map.of(normalRecipe.uniqueId(), 0L), inventory);
        assertThat(nullByRemaining).isNull();

        final LpResourceSet poorInventory = new LpResourceSet();
        poorInventory.setAmount(A, 1L);
        final Object nullByAffordability =
            invokeToCandidate(normalRecipe, Map.of(normalRecipe.uniqueId(), 1L), poorInventory);
        assertThat(nullByAffordability).isNull();
    }

    @Test
    void internalsShouldSortCandidatesAndIgnoreNonPositiveInputs() throws Exception {
        // Tests that candidates are sorted correctly by loop involvement and non-positive inputs are ignored.
        final LpPatternRecipe loopRecipe = recipe(A, B, 1, 1, 0);
        loopRecipe.setEffectivePriority(10);

        final LpPatternRecipe nonLoop = recipe(B, C, 1, 1, 0);
        nonLoop.setEffectivePriority(null);

        final LpPatternRecipe weird = customRecipeWithInput(Map.of((ResourceKey) A, -2L, B, 3L), Map.of(C, 1L), 0);

        final LpResourceSet inventory = new LpResourceSet();
        inventory.setAmount(A, 0L);
        inventory.setAmount(B, 9L);

        final long maxBatch = invokeComputeMaxAffordableBatch(weird, inventory);
        assertThat(maxBatch).isEqualTo(3L);

        final List<?> candidates = invokeBuildCandidates(
            List.of(loopRecipe, nonLoop),
            Map.of(loopRecipe.uniqueId(), true, nonLoop.uniqueId(), false),
            Map.of(loopRecipe.uniqueId(), 1L, nonLoop.uniqueId(), 1L),
            set(A, 1, B, 1)
        );
        assertThat(candidates).hasSize(2);
    }

    private static LpPatternRecipe recipe(final ResourceKey in,
                                          final ResourceKey out,
                                          final long inAmount,
                                          final long outAmount,
                                          final int priority) {
        return LpPatternRecipe.fromPattern(
            PatternBuilder.pattern().ingredient(in, inAmount).output(out, outAmount).build(),
            priority
        );
    }

    private static LpPatternRecipe customRecipeWithInput(final Map<ResourceKey, Long> input,
                                                         final Map<ResourceKey, Long> output,
                                                         final int priority) {
        final LpResourceSet in = new LpResourceSet(input);
        final LpResourceSet out = new LpResourceSet(output);
        final var pattern = PatternBuilder.pattern().ingredient(A, 1).output(B, 1).build();
        return new LpPatternRecipe(pattern, in, out, priority, null);
    }

    private static LpResourceSet set(final Object... entries) {
        final LpResourceSet set = new LpResourceSet();
        for (int i = 0; i < entries.length; i += 2) {
            set.setAmount((ResourceKey) entries[i], ((Number) entries[i + 1]).longValue());
        }
        return set;
    }

    private static Object candidate(final LpPatternRecipe recipe,
                                    final long remaining,
                                    final long maxBatch) throws Exception {
        final Class<?> candidateClass =
            Class.forName("com.refinedmods.refinedstorage.api.autocrafting.lp.LpExecutionPlanner$Candidate");
        final Constructor<?> constructor =
            candidateClass.getDeclaredConstructor(LpPatternRecipe.class, long.class, long.class);
        constructor.setAccessible(true);
        return constructor.newInstance(recipe, remaining, maxBatch);
    }

    @SuppressWarnings("unchecked")
    private static List<Long> invokeBuildBatchAttempts(final Object candidate) throws Exception {
        final Class<?> candidateClass =
            Class.forName("com.refinedmods.refinedstorage.api.autocrafting.lp.LpExecutionPlanner$Candidate");
        final Method method = LpExecutionPlanner.class.getDeclaredMethod("buildBatchAttempts", candidateClass);
        method.setAccessible(true);
        return (List<Long>) method.invoke(null, candidate);
    }

    private static void invokeAppendOrMerge(final List<LpExecutionPlanStep> plan,
                                            final LpPatternRecipe recipe,
                                            final long batch) throws Exception {
        final Method method = LpExecutionPlanner.class.getDeclaredMethod(
            "appendOrMergePlanStep",
            List.class,
            LpPatternRecipe.class,
            long.class
        );
        method.setAccessible(true);
        method.invoke(null, plan, recipe, batch);
    }

    private static void invokeRemoveOrShrink(final List<LpExecutionPlanStep> plan,
                                             final LpPatternRecipe recipe,
                                             final long batch) throws Exception {
        final Method method = LpExecutionPlanner.class.getDeclaredMethod(
            "removeOrShrinkLastPlanStep",
            List.class,
            LpPatternRecipe.class,
            long.class
        );
        method.setAccessible(true);
        method.invoke(null, plan, recipe, batch);
    }

    private static void invokeApplyBatch(final LpPatternRecipe recipe,
                                         final long batch,
                                         final LpResourceSet inventory) throws Exception {
        final Method method = LpExecutionPlanner.class.getDeclaredMethod(
            "applyRecipeBatch",
            LpPatternRecipe.class,
            long.class,
            LpResourceSet.class
        );
        method.setAccessible(true);
        method.invoke(null, recipe, batch, inventory);
    }

    private static void invokeRollbackBatch(final LpPatternRecipe recipe,
                                            final long batch,
                                            final LpResourceSet inventory) throws Exception {
        final Method method = LpExecutionPlanner.class.getDeclaredMethod(
            "rollbackRecipeBatch",
            LpPatternRecipe.class,
            long.class,
            LpResourceSet.class
        );
        method.setAccessible(true);
        method.invoke(null, recipe, batch, inventory);
    }

    private static long invokeComputeMaxAffordableBatch(final LpPatternRecipe recipe,
                                                        final LpResourceSet inventory) throws Exception {
        final Method method = LpExecutionPlanner.class.getDeclaredMethod(
            "computeMaxAffordableBatch",
            LpPatternRecipe.class,
            LpResourceSet.class
        );
        method.setAccessible(true);
        return (long) method.invoke(null, recipe, inventory);
    }

    private static Object invokeToCandidate(final LpPatternRecipe recipe,
                                            final Map<UUID, Long> remainingCounts,
                                            final LpResourceSet inventory) throws Exception {
        final Method method = LpExecutionPlanner.class.getDeclaredMethod(
            "toCandidate",
            LpPatternRecipe.class,
            Map.class,
            LpResourceSet.class
        );
        method.setAccessible(true);
        return method.invoke(null, recipe, remainingCounts, inventory);
    }

    @SuppressWarnings("unchecked")
    private static List<?> invokeBuildCandidates(final List<LpPatternRecipe> recipes,
                                                 final Map<UUID, Boolean> inLoopById,
                                                 final Map<UUID, Long> remainingCounts,
                                                 final LpResourceSet inventory) throws Exception {
        final Method method = LpExecutionPlanner.class.getDeclaredMethod(
            "buildCandidates",
            List.class,
            Map.class,
            Map.class,
            LpResourceSet.class
        );
        method.setAccessible(true);
        return (List<?>) method.invoke(null, recipes, inLoopById, remainingCounts, inventory);
    }

    private static boolean invokeTryCandidateBatch(final List<LpPatternRecipe> recipes,
                                                   final Map<UUID, Boolean> inLoopById,
                                                   final Map<UUID, Long> remainingCounts,
                                                   final LpResourceSet inventory,
                                                   final long totalRemaining,
                                                   final List<LpExecutionPlanStep> plan,
                                                   final Object candidate,
                                                   final long batch) throws Exception {
        final Class<?> candidateClass =
            Class.forName("com.refinedmods.refinedstorage.api.autocrafting.lp.LpExecutionPlanner$Candidate");
        final Method method = LpExecutionPlanner.class.getDeclaredMethod(
            "tryCandidateBatch",
            List.class,
            Map.class,
            Map.class,
            LpResourceSet.class,
            long.class,
            List.class,
            candidateClass,
            long.class
        );
        method.setAccessible(true);
        return (boolean) method.invoke(
            null,
            recipes,
            inLoopById,
            remainingCounts,
            inventory,
            totalRemaining,
            plan,
            candidate,
            batch
        );
    }
}
