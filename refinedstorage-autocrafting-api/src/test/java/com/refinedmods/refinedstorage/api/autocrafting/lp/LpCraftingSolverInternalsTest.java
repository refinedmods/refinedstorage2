package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static org.assertj.core.api.Assertions.assertThat;

class LpCraftingSolverInternalsTest {
    @Test
    void shouldExercisePrivateSetAndAttemptHelpers() throws Exception {
        final UUID a = UUID.randomUUID();
        final UUID b = UUID.randomUUID();

        final Set<UUID> bigger = invokeKeepLargerSet(Set.of(a), Set.of(a, b));
        final Set<UUID> smaller = invokeKeepLargerSet(Set.of(a, b), Set.of(a));

        assertThat(bigger).containsExactlyInAnyOrder(a, b);
        assertThat(smaller).containsExactlyInAnyOrder(a, b);

        final Set<List<UUID>> visited = new LinkedHashSet<>();
        visited.add(List.of());
        final ArrayDeque<Set<UUID>> attempts = new ArrayDeque<>();

        invokeAddAttemptIfUnseen(Set.of(a), a, visited, attempts);
        assertThat(attempts).isEmpty();

        invokeAddAttemptIfUnseen(Set.of(), a, visited, attempts);
        assertThat(attempts).hasSize(1);

        invokeAddAttemptIfUnseen(Set.of(), a, visited, attempts);
        assertThat(attempts).hasSize(1);
    }

    @Test
    void shouldExerciseComputeMaxCraftableTargetAmountPrivateMethod() throws Exception {
        final LpCraftingSolver solver = new LpCraftingSolver();
        final LpPatternRecipe recipe = LpPatternRecipe.fromPattern(
            PatternBuilder.pattern().ingredient(A, 1).output(B, 1).build(),
            0
        );

        final long emptyTarget = invokeComputeMaxCraftable(
            solver,
            List.of(recipe),
            set(A, 2),
            new LpResourceSet()
        );

        final long craftable = invokeComputeMaxCraftable(
            solver,
            List.of(recipe),
            set(A, 3),
            set(B, 1)
        );

        assertThat(emptyTarget).isZero();
        assertThat(craftable).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void shouldEnqueueCycleBreakAttemptFromMostUsedRecipe() throws Exception {
        final LpPatternRecipe recipeA = LpPatternRecipe.fromPattern(
            PatternBuilder.pattern().ingredient(A, 1).output(B, 1).build(),
            0
        );
        final LpPatternRecipe recipeB = LpPatternRecipe.fromPattern(
            PatternBuilder.pattern().ingredient(B, 1).output(A, 1).build(),
            0
        );

        final LpCraftingSolution solution = new LpCraftingSolution(
            java.util.Map.of(recipeA.uniqueId(), 2L, recipeB.uniqueId(), 5L),
            java.util.Map.of(),
            java.util.List.of()
        );

        final Set<List<UUID>> visited = new LinkedHashSet<>();
        visited.add(List.of());
        final ArrayDeque<Set<UUID>> attempts = new ArrayDeque<>();

        invokeEnqueueCycleBreakAttempts(
            List.of(List.of(recipeA, recipeB)),
            solution,
            Set.of(),
            visited,
            attempts
        );

        assertThat(attempts).hasSize(1);
        assertThat(attempts.peek()).containsExactly(recipeB.uniqueId());
    }

    private static LpResourceSet set(final Object... entries) {
        final LpResourceSet set = new LpResourceSet();
        for (int i = 0; i < entries.length; i += 2) {
            set.setAmount(
                (com.refinedmods.refinedstorage.api.resource.ResourceKey) entries[i],
                ((Number) entries[i + 1]).longValue()
            );
        }
        return set;
    }

    @SuppressWarnings("unchecked")
    private static Set<UUID> invokeKeepLargerSet(final Set<UUID> currentBest,
                                                 final Set<UUID> candidate) throws Exception {
        final Method method = LpCraftingSolver.class.getDeclaredMethod("keepLargerSet", Set.class, Set.class);
        method.setAccessible(true);
        return (Set<UUID>) method.invoke(null, currentBest, candidate);
    }

    private static void invokeAddAttemptIfUnseen(final Set<UUID> disabledRecipeIds,
                                                 final UUID recipeIdToDisable,
                                                 final Set<List<UUID>> visited,
                                                 final ArrayDeque<Set<UUID>> attempts) throws Exception {
        final Method method = LpCraftingSolver.class.getDeclaredMethod(
            "addAttemptIfUnseen",
            Set.class,
            UUID.class,
            Set.class,
            ArrayDeque.class
        );
        method.setAccessible(true);
        method.invoke(null, disabledRecipeIds, recipeIdToDisable, visited, attempts);
    }

    private static long invokeComputeMaxCraftable(final LpCraftingSolver solver,
                                                  final List<LpPatternRecipe> recipes,
                                                  final LpResourceSet startingResources,
                                                  final LpResourceSet target) throws Exception {
        final Method method = LpCraftingSolver.class.getDeclaredMethod(
            "computeMaxCraftableTargetAmount",
            List.class,
            LpResourceSet.class,
            LpResourceSet.class
        );
        method.setAccessible(true);
        return (long) method.invoke(solver, recipes, startingResources, target);
    }

    private static void invokeEnqueueCycleBreakAttempts(final List<List<LpPatternRecipe>> cycles,
                                                        final LpCraftingSolution solution,
                                                        final Set<UUID> disabledRecipeIds,
                                                        final Set<List<UUID>> visited,
                                                        final ArrayDeque<Set<UUID>> attempts) throws Exception {
        final Method method = LpCraftingSolver.class.getDeclaredMethod(
            "enqueueCycleBreakAttempts",
            List.class,
            LpCraftingSolution.class,
            Set.class,
            Set.class,
            ArrayDeque.class
        );
        method.setAccessible(true);
        method.invoke(null, cycles, solution, disabledRecipeIds, visited, attempts);
    }
}
