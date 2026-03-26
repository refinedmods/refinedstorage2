package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Java translations of the rs_crafter_sim {@code base_items_tests.rs} tests,
 * exercising {@link LpCraftingSolver#computeRequiredBaseItems}.
 *
 * <p>Each test corresponds 1:1 with a Rust {@code #[test]} in the Rust prototype.
 * Resource IDs are integers; recipes use the same (input, output, priority) shape.
 */
class RsCrafterSimBaseItemsTest {
    private final LpCraftingSolver solver = new LpCraftingSolver();

    // -------------------------------------------------------------------------
    // Infrastructure helpers

    private static ResourceKey item(final int id) {
        return new ItemId(id);
    }

    /**
     * Builds an {@link LpPatternRecipe} from flat int arrays.
     * {@code inputs}  – each element is {itemId, amount}
     * {@code outputs} – each element is {itemId, amount}
     */
    private static LpPatternRecipe recipe(final int[][] inputs,
                                          final int[][] outputs,
                                          final int priority) {
        var builder = pattern();
        for (final int[] input : inputs) {
            builder = builder.ingredient(item(input[0]), input[1]);
        }
        for (final int[] output : outputs) {
            builder = builder.output(item(output[0]), output[1]);
        }
        return LpPatternRecipe.fromPattern(builder.build(), priority);
    }

    /** Convenience shortcut for single-input, single-output recipes. */
    private static LpPatternRecipe single(final int inputId, final int inputAmt,
                                          final int outputId, final int outputAmt,
                                          final int priority) {
        return recipe(
            new int[][]{{inputId, inputAmt}},
            new int[][]{{outputId, outputAmt}},
            priority
        );
    }

    /**
     * Builds an {@link LpResourceSet} from flat (itemId, amount) pairs.
     * Zero amounts are skipped (same as Rust ItemSet semantics).
     */
    private static LpResourceSet resources(final int... idAmountFlat) {
        final LpResourceSet set = new LpResourceSet();
        for (int i = 0; i < idAmountFlat.length; i += 2) {
            if (idAmountFlat[i + 1] > 0) {
                set.addAmount(item(idAmountFlat[i]), idAmountFlat[i + 1]);
            }
        }
        return set;
    }

    /**
     * Asserts that {@code required} contains exactly the given (itemId, amount) pairs.
     * Mirrors the Rust {@code assert_required_base_items} helper.
     */
    private void assertRequiredBaseItems(final LpResourceSet required,
                                         final int... idAmountFlat) {
        assertThat(required.resourceKeys()).hasSize(idAmountFlat.length / 2);
        for (int i = 0; i < idAmountFlat.length; i += 2) {
            assertThat(required.getAmount(item(idAmountFlat[i])))
                .as("required count for item %d", idAmountFlat[i])
                .isEqualTo(idAmountFlat[i + 1]);
        }
    }

    // -------------------------------------------------------------------------
    // Tests – translated verbatim from base_items_tests.rs

    @Test
    void computeRequiredBaseItemsReportsSingleMissingBaseItem() {
        // Tests that missing base items are correctly identified in a crafting chain when starting inventory is insufficient.
        // Rust: compute_required_base_items_reports_single_missing_base_item
        // Chain item0→item1→item2; have 1x item0, need 2x item2 → 1 more item0 required.
        final List<LpPatternRecipe> recipes = List.of(
            single(0, 1, 1, 1, 0),
            single(1, 1, 2, 1, 0)
        );
        final LpResourceSet startingItems = resources(0, 1);
        final LpResourceSet target = resources(2, 2);

        final LpResourceSet required = solver.computeRequiredBaseItems(recipes, startingItems, target);

        assertRequiredBaseItems(required, 0, 1);
    }

    @Test
    void computeRequiredBaseItemsReturnsEmptyWhenStartingInventoryIsSufficient() {
        // Tests that no missing base items are reported when the starting inventory is sufficient to reach the target.
        // Rust: compute_required_base_items_returns_empty_when_starting_inventory_is_sufficient
        // Have 2x item0 – just enough to craft 2x item2 via two-step chain; nothing missing.
        final List<LpPatternRecipe> recipes = List.of(
            single(0, 1, 1, 1, 0),
            single(1, 1, 2, 1, 0)
        );
        final LpResourceSet startingItems = resources(0, 2);
        final LpResourceSet target = resources(2, 2);

        final LpResourceSet required = solver.computeRequiredBaseItems(recipes, startingItems, target);

        assertRequiredBaseItems(required /* empty */);
    }

    @Test
    void computeRequiredBaseItemsReportsMultipleMissingBaseItemsForMultiInputRecipe() {
        // Tests that multiple missing base items are correctly identified for recipes requiring multiple input types.
        // Rust: compute_required_base_items_reports_multiple_missing_base_items_for_multi_input_recipe
        // Recipe: 2x item0 + 1x item3 → 1x item2. Need 2x item2, have 2x item0 and 1x item3
        // (only enough for 1 craft); missing 2x item0 + 1x item3.
        final List<LpPatternRecipe> recipes = List.of(
            recipe(new int[][]{{0, 2}, {3, 1}}, new int[][]{{2, 1}}, 0)
        );
        final LpResourceSet startingItems = resources(0, 2, 3, 1);
        final LpResourceSet target = resources(2, 2);

        final LpResourceSet required = solver.computeRequiredBaseItems(recipes, startingItems, target);

        assertRequiredBaseItems(required, 0, 2, 3, 1);
    }

    @Test
    void computeRequiredBaseItemsHandlesNonProducibleTargetItemsWithRelevantRecipeGraph() {
        // Tests that target items that cannot be produced (base items) are correctly identified as missing requirements.
        // Rust: compute_required_base_items_handles_non_producible_target_items_with_relevant_recipe_graph
        // item7 is in the target but not producible by any recipe; only 3 more needed (have 1, need 4).
        final List<LpPatternRecipe> recipes = List.of(
            single(0, 1, 2, 1, 0)
        );
        final LpResourceSet startingItems = resources(0, 1, 7, 1);
        final LpResourceSet target = resources(2, 1, 7, 4);

        final LpResourceSet required = solver.computeRequiredBaseItems(recipes, startingItems, target);

        assertRequiredBaseItems(required, 7, 3);
    }

    @Test
    void computeRequiredBaseItemsLabelsNonProducibleTargetAsRequiredInput() {
        // Tests that when only base items are needed (no recipes available), they are correctly labeled as required inputs.
        // Rust: compute_required_base_items_labels_non_producible_target_as_required_input
        // No recipes at all; item7 is the target item; missing 3x item7 (have 1, need 4).
        final List<LpPatternRecipe> recipes = List.of();
        final LpResourceSet startingItems = resources(7, 1);
        final LpResourceSet target = resources(7, 4);

        final LpResourceSet required = solver.computeRequiredBaseItems(recipes, startingItems, target);

        assertRequiredBaseItems(required, 7, 3);
    }

    @Test
    void computeRequiredBaseItemsBreaksCycleBranchWhenTargetNotInCycle() {
        // Tests that base items required to break out of a cycle are identified when the target is outside the cycle.
        // Rust: compute_required_base_items_breaks_cycle_branch_when_target_not_in_cycle
        // Cycle: item0↔item1; item1→item2 (no cycle).
        // Target item2 is outside the cycle; requires 1x item0 to seed.
        final List<LpPatternRecipe> recipes = List.of(
            single(0, 1, 1, 1, 0),
            single(1, 1, 0, 1, 0),
            single(1, 1, 2, 1, 0)
        );
        final LpResourceSet startingItems = resources(/* empty */);
        final LpResourceSet target = resources(2, 1);

        final LpResourceSet required = solver.computeRequiredBaseItems(recipes, startingItems, target);

        assertRequiredBaseItems(required, 0, 1);
    }

    @Test
    void computeRequiredBaseItemsBreaksCycleBranchWhenTargetIsInCycle() {
        // Tests that base items required to break into a cycle are identified when the target is inside the cycle.
        // Rust: compute_required_base_items_breaks_cycle_branch_when_target_is_in_cycle
        // Cycle: item0↔item1. Target is item0 itself (inside the cycle).
        // Requires 1x item1 to enter the cycle and produce item0.
        final List<LpPatternRecipe> recipes = List.of(
            single(0, 1, 1, 1, 0),
            single(1, 1, 0, 1, 0)
        );
        final LpResourceSet startingItems = resources(/* empty */);
        final LpResourceSet target = resources(0, 1);

        final LpResourceSet required = solver.computeRequiredBaseItems(recipes, startingItems, target);

        assertRequiredBaseItems(required, 1, 1);
    }

    @Test
    void computeRequiredBaseItemsReportsLoopEntryAndMissingInputForUnstartableCycle() {
        // Tests that both loop entry items and missing base items are correctly identified with multiple targets and cycles.
        // Rust: compute_required_base_items_reports_loop_entry_and_missing_input_for_unstartable_cycle
        // Cycle: item0↔item1; unrelated item3→item2. Multi-target: item0 + item2.
        // Requires 1x item1 (loop entry) and 1x item3 (item2 base input).
        final List<LpPatternRecipe> recipes = List.of(
            single(0, 1, 1, 1, 0),
            single(1, 1, 0, 1, 0),
            single(3, 1, 2, 1, 0)
        );
        final LpResourceSet startingItems = resources(/* empty */);
        final LpResourceSet target = resources(0, 1, 2, 1);

        final LpResourceSet required = solver.computeRequiredBaseItems(recipes, startingItems, target);

        assertRequiredBaseItems(required, 1, 1, 3, 1);
    }

    /** Lightweight integer-keyed resource, equivalent to Rust's item-ID usize. */
    private record ItemId(int id) implements ResourceKey {
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof ItemId other && id == other.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
