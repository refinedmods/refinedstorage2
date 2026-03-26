package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static org.assertj.core.api.Assertions.assertThat;

class LpRecipeAnalysisInternalsTest {
    @Test
    void compareIndexCyclesAndCanonicalizeShouldBehaveDeterministically() throws Exception {
        // Tests that cycle comparison and canonicalization produce deterministic and consistent results.
        final int compareLeftRight = invokeCompareIndexCycles(List.of(0, 2), List.of(0, 3));
        final int compareLength = invokeCompareIndexCycles(List.of(1, 2), List.of(1, 2, 4));

        assertThat(compareLeftRight).isNegative();
        assertThat(compareLength).isNegative();

        final List<Integer> canonical = invokeCanonicalizeCycle(List.of(3, 5, 2));
        assertThat(canonical).containsExactly(2, 3, 5);
    }

    @Test
    void shouldEvaluateRecipePriorityAndInputPriorityPropagation() throws Exception {
        // Tests that recipe priority comparison correctly evaluates priority keys and propagates improved priorities through inputs.
        final LpPatternRecipe recipe = LpPatternRecipe.fromPattern(
            PatternBuilder.pattern().ingredient(A, 1).output(B, 1).build(),
            7
        );

        final LpRecipePriorityKey base = new LpRecipePriorityKey();
        final LpRecipePriorityKey candidate = base.appendRecipePriority(recipe);

        final Map<java.util.UUID, LpRecipePriorityKey> best = new LinkedHashMap<>();

        assertThat(invokeIsBetterRecipePriority(recipe, candidate, best)).isTrue();
        best.put(recipe.uniqueId(), candidate);
        assertThat(invokeIsBetterRecipePriority(recipe, candidate, best))
            .isFalse();

        final Map<ResourceKey, LpRecipePriorityKey> bestResources = new LinkedHashMap<>();
        final ArrayDeque<ResourceKey> stack = new ArrayDeque<>();
        invokePushImprovedInputResources(recipe, candidate, bestResources, stack);

        assertThat(bestResources).containsKey(A);
        assertThat(stack).contains(A);

        final int before = stack.size();
        invokePushImprovedInputResources(recipe, candidate, bestResources, stack);
        assertThat(stack).hasSize(before);
    }

    private static int invokeCompareIndexCycles(final List<Integer> left,
                                                final List<Integer> right) throws Exception {
        final Method method = LpRecipeAnalysis.class.getDeclaredMethod("compareIndexCycles", List.class, List.class);
        method.setAccessible(true);
        return (int) method.invoke(null, left, right);
    }

    @SuppressWarnings("unchecked")
    private static List<Integer> invokeCanonicalizeCycle(final List<Integer> cycle) throws Exception {
        final Method method = LpRecipeAnalysis.class.getDeclaredMethod("canonicalizeCycle", List.class);
        method.setAccessible(true);
        return (List<Integer>) method.invoke(null, cycle);
    }

    private static boolean invokeIsBetterRecipePriority(final LpPatternRecipe recipe,
                                                        final LpRecipePriorityKey candidate,
                                                        final Map<java.util.UUID, LpRecipePriorityKey> best)
        throws Exception {
        final Method method = LpRecipeAnalysis.class.getDeclaredMethod(
            "isBetterRecipePriority",
            LpPatternRecipe.class,
            LpRecipePriorityKey.class,
            Map.class
        );
        method.setAccessible(true);
        return (boolean) method.invoke(null, recipe, candidate, best);
    }

    private static void invokePushImprovedInputResources(final LpPatternRecipe recipe,
                                                         final LpRecipePriorityKey candidate,
                                                         final Map<ResourceKey, LpRecipePriorityKey> bestResources,
                                                         final ArrayDeque<ResourceKey> stack) throws Exception {
        final Method method = LpRecipeAnalysis.class.getDeclaredMethod(
            "pushImprovedInputResources",
            LpPatternRecipe.class,
            LpRecipePriorityKey.class,
            Map.class,
            ArrayDeque.class
        );
        method.setAccessible(true);
        method.invoke(null, recipe, candidate, bestResources, stack);
    }
}
