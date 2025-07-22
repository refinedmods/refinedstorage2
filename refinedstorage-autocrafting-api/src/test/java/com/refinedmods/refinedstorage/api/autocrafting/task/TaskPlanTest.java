package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorImpl;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingUtil.patterns;
import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingUtil.storage;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.CRAFTING_TABLE_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.OAK_PLANKS_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.SPRUCE_PLANKS_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.CRAFTING_TABLE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlanCraftingCalculatorListener.calculatePlan;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskPlanTest {
    @Test
    void shouldNotPlanTaskWhenThereAreMissingResources() {
        // Arrange
        final RootStorage storage = storage();
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN);
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final Optional<TaskPlan> optionalPlan = calculatePlan(sut, OAK_PLANKS, 1, CancellationToken.NONE);

        // Assert
        assertThat(optionalPlan).isEmpty();
    }

    @Test
    void shouldPlanTaskWithCorrectResourceAndAmount() {
        // Arrange
        final RootStorage storage = storage(new ResourceAmount(OAK_LOG, 1));
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN);
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final Optional<TaskPlan> optionalPlan = calculatePlan(sut, OAK_PLANKS, 1, CancellationToken.NONE);

        // Assert
        assertThat(optionalPlan).isPresent();
        final TaskPlan plan = optionalPlan.get();
        assertThat(plan.rootPattern()).isEqualTo(OAK_PLANKS_PATTERN);
        assertThat(plan.resource()).isEqualTo(OAK_PLANKS);
        assertThat(plan.amount()).isEqualTo(4);
    }

    @Test
    void testPlanTaskWithIngredientsUsedFromRootStorageAndInternalStorageWithChildPattern() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final Optional<TaskPlan> optionalPlan = calculatePlan(sut, CRAFTING_TABLE, 3, CancellationToken.NONE);

        // Assert
        assertThat(optionalPlan).isPresent();
        final TaskPlan plan = optionalPlan.get();
        assertThat(plan.rootPattern()).isEqualTo(CRAFTING_TABLE_PATTERN);
        assertThat(plan.resource()).isEqualTo(CRAFTING_TABLE);
        assertThat(plan.amount()).isEqualTo(3);
        assertThat(plan.initialRequirements()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(OAK_PLANKS, 4),
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1)
        );
        assertThat(plan.patterns()).containsOnlyKeys(CRAFTING_TABLE_PATTERN, OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN);
        assertThat(plan.getPattern(CRAFTING_TABLE_PATTERN))
            .usingRecursiveComparison()
            .isEqualTo(new TaskPlan.PatternPlan(true, 3, Map.of(
                0, Map.of(OAK_PLANKS, 3L),
                1, Map.of(OAK_PLANKS, 3L),
                2, Map.of(OAK_PLANKS, 2L, SPRUCE_PLANKS, 1L),
                3, Map.of(SPRUCE_PLANKS, 3L)
            )));
        assertThat(plan.getPattern(OAK_PLANKS_PATTERN))
            .usingRecursiveComparison()
            .isEqualTo(new TaskPlan.PatternPlan(false, 1, Map.of(
                0, Map.of(OAK_LOG, 1L)
            )));
        assertThat(plan.getPattern(SPRUCE_PLANKS_PATTERN))
            .usingRecursiveComparison()
            .isEqualTo(new TaskPlan.PatternPlan(false, 1, Map.of(
                0, Map.of(SPRUCE_LOG, 1L)
            )));
    }

    @Test
    void shouldNotModifyPlan() {
        // Arrange
        final ResourceAmount oakLog = new ResourceAmount(OAK_LOG, 1);
        final RootStorage storage = storage(
            oakLog,
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final Optional<TaskPlan> optionalPlan = calculatePlan(sut, CRAFTING_TABLE, 3, CancellationToken.NONE);

        // Assert
        assertThat(optionalPlan).isPresent();
        final TaskPlan plan = optionalPlan.get();

        final Collection<ResourceAmount> initialRequirements = plan.initialRequirements();
        assertThatThrownBy(() -> initialRequirements.add(oakLog))
            .isInstanceOf(UnsupportedOperationException.class);
        final Map<Pattern, TaskPlan.PatternPlan> planPatterns = plan.patterns();
        assertThatThrownBy(planPatterns::clear)
            .isInstanceOf(UnsupportedOperationException.class);
        final Map<Integer, Map<ResourceKey, Long>> craftingTableIngredients =
            plan.getPattern(CRAFTING_TABLE_PATTERN).ingredients();
        assertThatThrownBy(craftingTableIngredients::clear)
            .isInstanceOf(UnsupportedOperationException.class);
        final Map<ResourceKey, Long> firstIngredient = craftingTableIngredients.get(0);
        assertThatThrownBy(() -> firstIngredient.put(OAK_LOG, 1L))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
