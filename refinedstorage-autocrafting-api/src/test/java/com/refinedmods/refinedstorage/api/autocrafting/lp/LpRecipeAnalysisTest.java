package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.X;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.Y;
import static org.assertj.core.api.Assertions.assertThat;

class LpRecipeAnalysisTest {
    @Test
    void shouldCollectRelevantResourceKeys() {
        // Tests that relevant resource keys are collected from recipes and target items, including base items.
        final List<LpPatternRecipe> recipes = List.of(
            recipe(A, B, 1, 1, 0),
            recipe(B, C, 1, 1, 0)
        );
        final LpResourceSet target = set(B, 1);

        final Set<ResourceKey> relevant = LpRecipeAnalysis.collectRelevantResourceKeys(recipes, target, Set.of(X));

        assertThat(relevant).containsExactlyInAnyOrder(A, B, C, X);
    }

    @Test
    void shouldPrioritizeAndPruneRecipes() {
        // Tests that recipes are prioritized by priority value and pruned to only those relevant to the target.
        final LpPatternRecipe lowPriority = recipe(A, B, 1, 1, 1);
        final LpPatternRecipe highPriority = recipe(A, B, 1, 1, 10);
        final LpPatternRecipe unrelated = recipe(X, Y, 1, 1, 99);

        final LpRecipeAnalysis.PrioritizedRecipeSet result =
            LpRecipeAnalysis.prioritizeAndPruneRelevantRecipesAndItems(
                List.of(lowPriority, highPriority, unrelated),
                set(B, 1)
            );

        assertThat(result.recipes()).hasSize(2);
        assertThat(result.recipes().getFirst().basePriority()).isEqualTo(10);
        assertThat(result.recipes().get(1).basePriority()).isEqualTo(1);
        assertThat(result.recipes().getFirst().effectivePriority())
            .isGreaterThan(result.recipes().get(1).effectivePriority());
        assertThat(result.recipes().get(1).effectivePriority()).isZero();
        assertThat(result.relevantResourceKeys()).contains(A, B);
        assertThat(result.relevantResourceKeys()).doesNotContain(X, Y);
    }

    @Test
    void shouldCollectNonProducibleResources() {
        // Tests that resources that cannot be produced by any recipe are correctly identified.
        final Set<ResourceKey> nonProducible = LpRecipeAnalysis.collectNonProducibleResources(
            List.of(recipe(A, B, 1, 1, 0)),
            Set.of(A, B, C)
        );

        assertThat(nonProducible).containsExactlyInAnyOrder(A, C);
    }

    @Test
    void shouldSelectTopPriorityRecipesPerOutputResource() {
        // Tests that for each output resource, only the highest priority recipe is selected.
        final LpPatternRecipe topB = recipe(A, B, 1, 1, 0);
        final LpPatternRecipe lowB = recipe(A, B, 2, 1, 0);
        final LpPatternRecipe topC = recipe(B, C, 1, 1, 0);

        lowB.setEffectivePriority(0);
        topB.setEffectivePriority(10);
        topC.setEffectivePriority(1);

        final List<LpPatternRecipe> selected = LpRecipeAnalysis.selectTopPriorityRecipesPerOutputResource(
            List.of(lowB, topC, topB)
        );

        assertThat(selected).hasSize(2);
        assertThat(selected).extracting(LpPatternRecipe::uniqueId)
            .containsExactly(topB.uniqueId(), topC.uniqueId());
    }

    @Test
    void shouldDetectCyclesAndInLoopFlags() {
        // Tests that recipe cycles are detected and recipes are correctly flagged as being in a cycle.
        final LpPatternRecipe ab = recipe(A, B, 1, 1, 0);
        final LpPatternRecipe ba = recipe(B, A, 1, 1, 0);
        final LpPatternRecipe bc = recipe(B, C, 1, 1, 0);

        final LpRecipeAnalysis.CycleDetectionResult result = LpRecipeAnalysis.detectRecipeCycles(List.of(ab, ba, bc));

        assertThat(result.cycles()).isNotEmpty();
        assertThat(result.inLoopByRecipeId().get(ab.uniqueId())).isTrue();
        assertThat(result.inLoopByRecipeId().get(ba.uniqueId())).isTrue();
        assertThat(result.inLoopByRecipeId().get(bc.uniqueId())).isFalse();
    }

    @Test
    void shouldCollectLoopClosingAndLoopEntryResourcesOnTargetBranches() {
        // Tests that recipes that close cycles and resources that enter cycles are correctly identified on target branches.
        final LpPatternRecipe ab = recipe(A, B, 1, 1, 0);
        final LpPatternRecipe ba = recipe(B, A, 1, 1, 0);

        final Set<java.util.UUID> loopClosing = LpRecipeAnalysis.collectLoopClosingRecipeIdsOnTargetBranches(
            List.of(ab, ba),
            set(B, 1)
        );
        final Set<ResourceKey> loopEntryDeficits = LpRecipeAnalysis.collectLoopEntryDeficitResourcesOnTargetBranches(
            List.of(ab, ba),
            set(B, 1)
        );

        assertThat(loopClosing).contains(ba.uniqueId());
        assertThat(loopEntryDeficits).contains(A);
    }

    @Test
    void shouldCanonicalizeAndSortDetectedCycles() {
        // Tests that detected cycles are canonicalized and sorted into a consistent order for analysis.
        final LpPatternRecipe ab = recipe(A, B, 1, 1, 0); // index 0
        final LpPatternRecipe ba = recipe(B, A, 1, 1, 0); // index 1
        final LpPatternRecipe cx = recipe(C, X, 1, 1, 0); // index 2
        final LpPatternRecipe xc = recipe(X, C, 1, 1, 0); // index 3

        final LpRecipeAnalysis.CycleDetectionResult result =
            LpRecipeAnalysis.detectRecipeCycles(List.of(ab, ba, cx, xc));

        assertThat(result.cycles()).hasSize(2);
        assertThat(result.cycles().getFirst()).extracting(LpPatternRecipe::uniqueId)
            .containsExactly(ab.uniqueId(), ba.uniqueId());
        assertThat(result.cycles().get(1)).extracting(LpPatternRecipe::uniqueId)
            .containsExactly(cx.uniqueId(), xc.uniqueId());
    }

    private static LpPatternRecipe recipe(final ResourceKey input,
                                          final ResourceKey output,
                                          final long inputAmount,
                                          final long outputAmount,
                                          final int priority) {
        return LpPatternRecipe.fromPattern(
            pattern().ingredient(input, inputAmount).output(output, outputAmount).build(),
            priority
        );
    }

    private static LpResourceSet set(final ResourceKey resource, final long amount) {
        final LpResourceSet set = new LpResourceSet();
        set.setAmount(resource, amount);
        return set;
    }
}
