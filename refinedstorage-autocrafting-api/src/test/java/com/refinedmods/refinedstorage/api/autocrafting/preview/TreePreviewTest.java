package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.CancelledCancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorImpl;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingUtil.patterns;
import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingUtil.storage;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.SIGN_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.STICKS_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.CRAFTING_TABLE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SIGN;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.STICKS;
import static com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreviewBuilder.tree;
import static com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreviewCraftingCalculatorListener.calculateTree;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TreePreviewTest {
    @Test
    void shouldNotCalculateForPatternThatIsNotFound() {
        // Arrange
        final RootStorage storage = storage();
        final PatternRepository patterns = patterns();
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final Executable action = () -> calculateTree(sut, CRAFTING_TABLE, 1, CancellationToken.NONE);

        // Assert
        final IllegalStateException e = assertThrows(IllegalStateException.class, action);
        assertThat(e).hasMessage("No pattern found for " + CRAFTING_TABLE);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void shouldNotCalculateWithInvalidRequestedAmount(final long requestedAmount) {
        // Arrange
        final RootStorage storage = storage();
        final PatternRepository patterns = patterns();
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final Executable action = () -> calculateTree(sut, CRAFTING_TABLE, requestedAmount, CancellationToken.NONE);

        // Assert
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, action);
        assertThat(e).hasMessage("Requested amount must be greater than 0");
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2})
    void shouldCalculateForSingleRootPatternSingleIngredientAndAllResourcesAreAvailable(
        final long requestedAmount
    ) {
        // Arrange
        final RootStorage storage = storage(new ResourceAmount(OAK_PLANKS, 8));
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_PLANKS, 4)
                .output(CRAFTING_TABLE, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, CRAFTING_TABLE, requestedAmount, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(tree(PreviewType.SUCCESS, CRAFTING_TABLE, requestedAmount)
            .node(OAK_PLANKS, 4 * requestedAmount).available(4 * requestedAmount).end()
            .build());
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2})
    void shouldCalculateForSingleRootPatternSingleIngredientSpreadOutOverMultipleIngredientsAndThereAreMissingResources(
        final long requestedAmount
    ) {
        // Arrange
        final RootStorage storage = storage();
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_LOG, 1)
                .output(OAK_PLANKS, 4)
                .build(),
            pattern()
                .ingredient(OAK_PLANKS, 1)
                .ingredient(OAK_PLANKS, 1)
                .ingredient(OAK_PLANKS, 1)
                .ingredient(OAK_PLANKS, 1)
                .output(CRAFTING_TABLE, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, CRAFTING_TABLE, requestedAmount, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison()
            .isEqualTo(tree(PreviewType.MISSING_RESOURCES, CRAFTING_TABLE, requestedAmount)
                .node(OAK_PLANKS, 4 * requestedAmount).toCraft(4 * requestedAmount)
                .node(OAK_LOG, requestedAmount).missing(requestedAmount)
                .end()
                .end().build());
    }

    @Test
    void shouldNotCalculateForSingleRootPatternSingleIngredientAndAlmostAllResourcesAreAvailable() {
        // Arrange
        final RootStorage storage = storage(new ResourceAmount(OAK_PLANKS, 8));
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_PLANKS, 4)
                .output(CRAFTING_TABLE, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, CRAFTING_TABLE, 3, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(tree(PreviewType.MISSING_RESOURCES, CRAFTING_TABLE, 3)
            .node(OAK_PLANKS, 3 * 4).available(8).missing(4).end()
            .build());
    }

    @Test
    void shouldCalculateWithSingleRootPatternWithMultipleIngredientAndMultipleAreCraftableButOnly1HasEnoughResources() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(SPRUCE_LOG, 1)
        );
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_LOG, 1)
                .output(OAK_PLANKS, 4)
                .build(),
            pattern()
                .ingredient(SPRUCE_LOG, 1)
                .output(SPRUCE_PLANKS, 4)
                .build(),
            pattern()
                .ingredient(4).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
                .output(CRAFTING_TABLE, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, CRAFTING_TABLE, 1, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(tree(PreviewType.SUCCESS, CRAFTING_TABLE, 1)
            .node(SPRUCE_PLANKS, 4).toCraft(4).node(SPRUCE_LOG, 1).available(1).end().end()
            .build());
    }

    @Test
    void shouldPrioritizeResourcesThatWeHaveMostOfInStorageForSingleRootPatternAndMultipleIngredients() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_PLANKS, 4 * 10),
            new ResourceAmount(SPRUCE_PLANKS, 4 * 5)
        );
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(4).input(SPRUCE_PLANKS).input(OAK_PLANKS).end()
                .output(CRAFTING_TABLE, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, CRAFTING_TABLE, 11, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(tree(PreviewType.SUCCESS, CRAFTING_TABLE, 11)
            .node(OAK_PLANKS, 4 * 10).available(4 * 10).end()
            .node(SPRUCE_PLANKS, 4).available(4).end()
            .build());
    }

    @Test
    void shouldExhaustAllPossibleIngredientsWhenRunningOutInSingleRootPatternAndMultipleIngredients() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_PLANKS, 4 * 10),
            new ResourceAmount(SPRUCE_PLANKS, 4 * 5)
        );
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(4).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
                .output(CRAFTING_TABLE, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, CRAFTING_TABLE, 16, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(tree(PreviewType.MISSING_RESOURCES, CRAFTING_TABLE, 16)
            .node(OAK_PLANKS, 4 * 10).available(4 * 10).end()
            .node(SPRUCE_PLANKS, (4 * 5) + 4).available(4 * 5).missing(4).end()
            .build());
    }

    @Test
    void shouldExhaustAllPossibleIngredientsWhenRunningOutInSingleRootPatternAndMultipleCraftableIngredients() {
        // Arrange
        final RootStorage storage = storage();
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_LOG, 1)
                .output(OAK_PLANKS, 4)
                .build(),
            pattern()
                .ingredient(SPRUCE_LOG, 1)
                .output(SPRUCE_PLANKS, 4)
                .build(),
            pattern()
                .ingredient(4).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
                .output(CRAFTING_TABLE, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, CRAFTING_TABLE, 1, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(tree(PreviewType.MISSING_RESOURCES, CRAFTING_TABLE, 1)
            .node(SPRUCE_PLANKS, 4).toCraft(4).node(SPRUCE_LOG, 1).missing(1).end().end()
            .build());
    }

    @Test
    void shouldCalculateForMultipleRootPatternsAndSingleIngredientAndAllResourcesAreAvailable() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(SPRUCE_PLANKS, 8)
        );
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_PLANKS, 4)
                .output(CRAFTING_TABLE, 1)
                .build(),
            pattern()
                .ingredient(SPRUCE_PLANKS, 4)
                .output(CRAFTING_TABLE, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, CRAFTING_TABLE, 2, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(tree(PreviewType.SUCCESS, CRAFTING_TABLE, 2)
            .node(SPRUCE_PLANKS, 4 * 2).available(8).end()
            .build());
    }

    @Test
    void shouldNotCalculateForMultipleRootPatternsAndSingleIngredientAndAlmostAllResourcesAreAvailable() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(SPRUCE_PLANKS, 8)
        );
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_PLANKS, 4)
                .output(CRAFTING_TABLE, 1)
                .build(),
            pattern()
                .ingredient(SPRUCE_PLANKS, 8)
                .output(CRAFTING_TABLE, 2)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, CRAFTING_TABLE, 3, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(tree(PreviewType.MISSING_RESOURCES, CRAFTING_TABLE, 4)
            .node(SPRUCE_PLANKS, 16).available(8).missing(8).end()
            .build());
    }

    @Test
    void shouldCalculateForSingleRootPatternAndSingleChildPatternWithSingleIngredientAndAllResourcesAreAvailable() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_PLANKS, 3),
            new ResourceAmount(OAK_LOG, 3)
        );
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_LOG, 1)
                .output(OAK_PLANKS, 4)
                .build(),
            pattern()
                .ingredient(OAK_PLANKS, 4)
                .output(CRAFTING_TABLE, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, CRAFTING_TABLE, 3, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(tree(PreviewType.SUCCESS, CRAFTING_TABLE, 3)
            .node(OAK_PLANKS, 12).toCraft(12).available(3)
            .node(OAK_LOG, 3).available(3).end()
            .end()
            .build());
    }

    @Test
    void shouldNotCalculateForSingleRootPatternSingleChildPatternWSingleIngredientAndAlmostAllResourcesAreAvailable() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 2)
        );
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_LOG, 1)
                .output(OAK_PLANKS, 4)
                .build(),
            pattern()
                .ingredient(SPRUCE_LOG, 1)
                .output(OAK_PLANKS, 4)
                .build(),
            pattern()
                .ingredient(OAK_PLANKS, 4)
                .output(CRAFTING_TABLE, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, CRAFTING_TABLE, 3, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(tree(PreviewType.MISSING_RESOURCES, CRAFTING_TABLE, 3)
            .node(OAK_PLANKS, 12).toCraft(12)
            .node(SPRUCE_LOG, 3).missing(3)
            .end().end()
            .build());
    }

    @Test
    void shouldCraftMoreIfNecessaryIfResourcesFromInternalStorageAreUsedUp() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 4)
        );
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_LOG, 1)
                .output(OAK_PLANKS, 2)
                .build(),
            pattern()
                .ingredient(OAK_PLANKS, 4)
                .output(CRAFTING_TABLE, 1)
                .build(),
            STICKS_PATTERN,
            SIGN_PATTERN
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, SIGN, 1, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(tree(PreviewType.SUCCESS, SIGN, 3)
            .node(OAK_PLANKS, 6).toCraft(6).node(OAK_LOG, 3).available(3).end().end()
            .node(STICKS, 1).toCraft(4).node(OAK_PLANKS, 2).toCraft(2)
            .node(OAK_LOG, 1).available(1).end().end()
            .end().build());
    }

    @Test
    void shouldCraftMoreIfNecessaryIfResourcesFromStorageAreUsedUp() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_PLANKS, 6),
            new ResourceAmount(OAK_LOG, 1)
        );
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_LOG, 1)
                .output(OAK_PLANKS, 2)
                .build(),
            pattern()
                .ingredient(OAK_PLANKS, 4)
                .output(CRAFTING_TABLE, 1)
                .build(),
            STICKS_PATTERN,
            SIGN_PATTERN
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, SIGN, 1, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(tree(PreviewType.SUCCESS, SIGN, 3)
            .node(OAK_PLANKS, 6).available(6).end()
            .node(STICKS, 1).toCraft(4).node(OAK_PLANKS, 2).toCraft(2).node(OAK_LOG, 1).available(1).end().end()
            .end().build());
    }

    private static Stream<Arguments> provideMissingResourcesPreview() {
        return Stream.of(
            Arguments.of(1, tree(PreviewType.MISSING_RESOURCES, SIGN, 3)
                .node(OAK_PLANKS, 6).toCraft(6).node(OAK_LOG, 3).available(3).end().end()
                .node(STICKS, 1).toCraft(4).node(OAK_PLANKS, 2).toCraft(2).node(OAK_LOG, 1).missing(1).end().end()
                .end().build()),
            Arguments.of(4, tree(PreviewType.MISSING_RESOURCES, SIGN, 6)
                .node(OAK_PLANKS, 12).toCraft(12).node(OAK_LOG, 6).available(3).missing(3).end().end()
                .node(STICKS, 2).toCraft(4).node(OAK_PLANKS, 2).toCraft(2).node(OAK_LOG, 1).missing(1).end().end()
                .end().build()),
            Arguments.of(20, tree(PreviewType.MISSING_RESOURCES, SIGN, 21)
                .node(OAK_PLANKS, 6 * 7).toCraft(6 * 7).node(OAK_LOG, 21).available(3).missing(21 - 3).end().end()
                .node(STICKS, 7).toCraft(8).node(OAK_PLANKS, 4).toCraft(4).node(OAK_LOG, 2).missing(2).end().end()
                .end().build())
        );
    }

    @ParameterizedTest
    @MethodSource("provideMissingResourcesPreview")
    void shouldKeepCalculatingEvenIfResourcesAreMissing(final long requestedAmount,
                                                        final TreePreview expectedTree) {
        // Arrange
        final RootStorage storage = storage(new ResourceAmount(OAK_LOG, 3));
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_LOG, 1)
                .output(OAK_PLANKS, 2)
                .build(),
            pattern()
                .ingredient(OAK_PLANKS, 4)
                .output(CRAFTING_TABLE, 1)
                .build(),
            STICKS_PATTERN,
            SIGN_PATTERN
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, SIGN, requestedAmount, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(expectedTree);
    }

    private static Stream<Arguments> provideAmounts() {
        return Stream.of(
            Arguments.arguments(1, 4, 1),
            Arguments.arguments(2, 4, 1),
            Arguments.arguments(3, 4, 1),
            Arguments.arguments(4, 4, 1),
            Arguments.arguments(5, 8, 2),
            Arguments.arguments(6, 8, 2)
        );
    }

    @ParameterizedTest
    @MethodSource("provideAmounts")
    void shouldCraftCorrectAmountWhenNotRequestingAMultipleOfThePatternOutputAmount(
        final long requestedAmount,
        final long planksCrafted,
        final long logsUsed
    ) {
        // Arrange
        final RootStorage storage = storage(new ResourceAmount(OAK_LOG, 30));
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_LOG, 1)
                .output(OAK_PLANKS, 2)
                .output(OAK_PLANKS, 2)
                .output(STICKS, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, OAK_PLANKS, requestedAmount, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(tree(PreviewType.SUCCESS, OAK_PLANKS, planksCrafted)
            .node(OAK_LOG, logsUsed).available(logsUsed).end()
            .build());
    }

    @Test
    void shouldDetectPatternCycles() {
        // Arrange
        final RootStorage storage = storage();
        final Pattern cycledPattern = pattern()
            .ingredient(OAK_LOG, 1)
            .output(OAK_PLANKS, 4)
            .build();
        final PatternRepository patterns = patterns(
            cycledPattern,
            pattern()
                .ingredient(OAK_PLANKS, 4)
                .output(OAK_LOG, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, OAK_PLANKS, 1, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(new TreePreview(
            PreviewType.CYCLE_DETECTED,
            null,
            cycledPattern.layout().outputs()
        ));
    }

    @Test
    void shouldDetectNumberOverflowInIngredient() {
        // Arrange
        final RootStorage storage = storage();
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_LOG, Long.MAX_VALUE)
                .output(OAK_PLANKS, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, OAK_PLANKS, 2, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(new TreePreview(
            PreviewType.OVERFLOW, null, Collections.emptyList()
        ));
    }

    @Test
    void shouldDetectNumberOverflowWithRootPattern() {
        // Arrange
        final RootStorage storage = storage();
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_LOG, 1)
                .output(OAK_PLANKS, 4)
                .build(),
            pattern()
                .ingredient(OAK_PLANKS, 4)
                .output(CRAFTING_TABLE, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, OAK_PLANKS, Long.MAX_VALUE, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(new TreePreview(
            PreviewType.OVERFLOW, null, Collections.emptyList()
        ));
    }

    @Test
    void shouldDetectNumberOverflowWithOutputOfChildPattern() {
        // Arrange
        final RootStorage storage = storage();
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_LOG, 1)
                .output(OAK_PLANKS, 4)
                .output(SIGN, Long.MAX_VALUE)
                .build(),
            pattern()
                .ingredient(OAK_PLANKS, 4)
                .output(CRAFTING_TABLE, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, CRAFTING_TABLE, 2, CancellationToken.NONE);

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(new TreePreview(
            PreviewType.OVERFLOW, null, Collections.emptyList()
        ));
    }

    @Test
    void shouldCancel() {
        // Arrange
        final RootStorage storage = storage();
        final PatternRepository patterns = patterns(
            pattern()
                .ingredient(OAK_LOG, 1)
                .output(OAK_PLANKS, 4)
                .build(),
            pattern()
                .ingredient(OAK_PLANKS, 4)
                .output(CRAFTING_TABLE, 1)
                .build()
        );
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final TreePreview tree = calculateTree(sut, CRAFTING_TABLE, 2, new CancelledCancellationToken());

        // Assert
        assertThat(tree).usingRecursiveComparison().isEqualTo(new TreePreview(
            PreviewType.CANCELLED, null, Collections.emptyList()
        ));
    }
}
