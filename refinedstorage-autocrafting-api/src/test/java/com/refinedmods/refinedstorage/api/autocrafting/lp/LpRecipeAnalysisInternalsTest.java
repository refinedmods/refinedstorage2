package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.lang.reflect.Method;
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
        // Recipe priorities should evaluate and propagate to inputs.
        final LpPatternRecipe recipe = LpPatternRecipe.fromPattern(
            PatternBuilder.pattern().ingredient(A, 1).output(B, 1).build(),
            7
        );

        final LpRecipePriorityKey base = new LpRecipePriorityKey();
        final LpRecipePriorityKey candidate = base.appendRecipePriority(recipe);

        final Map<java.util.UUID, LpRecipePriorityKey> best = new LinkedHashMap<>();

        assertThat(invokeTryUpdateBestRecipePriority(recipe, candidate, best)).isTrue();
        assertThat(best).containsEntry(recipe.uniqueId(), candidate);
        assertThat(invokeTryUpdateBestRecipePriority(recipe, candidate, best))
            .isFalse();

        final LpResourceSet target = new LpResourceSet();
        target.setAmount(B, 1);
        final List<LpPatternRecipe> prioritized =
            LpRecipeAnalysis.prioritizeAndPruneRelevantRecipes(List.of(recipe), target);
        final java.util.Set<ResourceKey> relevant = LpRecipeAnalysis.collectRelevantResourceKeys(prioritized);

        assertThat(prioritized).hasSize(1);
        assertThat(prioritized.getFirst().uniqueId()).isEqualTo(recipe.uniqueId());
        assertThat(relevant).contains(A, B);
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

    private static boolean invokeTryUpdateBestRecipePriority(final LpPatternRecipe recipe,
                                                             final LpRecipePriorityKey candidate,
                                                             final Map<java.util.UUID, LpRecipePriorityKey> best)
        throws Exception {
        final Method method = LpRecipeAnalysis.class.getDeclaredMethod(
            "tryUpdateBestRecipePriority",
            java.util.UUID.class,
            LpRecipePriorityKey.class,
            Map.class
        );
        method.setAccessible(true);
        return (boolean) method.invoke(null, recipe.uniqueId(), candidate, best);
    }

}
