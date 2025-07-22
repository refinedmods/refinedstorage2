package com.refinedmods.refinedstorage.api.autocrafting.preview;

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
import static com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewCraftingCalculatorListener.calculatePreview;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PreviewTest {
    @Test
    void shouldNotCalculateForPatternThatIsNotFound() {
        // Arrange
        final RootStorage storage = storage();
        final PatternRepository patterns = patterns();
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final Executable action = () -> calculatePreview(sut, CRAFTING_TABLE, 1, CancellationToken.NONE);

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
        final Executable action = () -> calculatePreview(sut, CRAFTING_TABLE, requestedAmount, CancellationToken.NONE);

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
        final Preview preview = calculatePreview(sut, CRAFTING_TABLE, requestedAmount, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison().isEqualTo(PreviewBuilder.create()
            .addToCraft(CRAFTING_TABLE, requestedAmount)
            .addAvailable(OAK_PLANKS, requestedAmount * 4)
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
        final Preview preview = calculatePreview(sut, CRAFTING_TABLE, requestedAmount, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison().isEqualTo(PreviewBuilder.create()
            .addToCraft(CRAFTING_TABLE, requestedAmount)
            .addToCraft(OAK_PLANKS, requestedAmount * 4)
            .addMissing(OAK_LOG, requestedAmount)
            .build());
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
        final Preview preview = calculatePreview(sut, CRAFTING_TABLE, 3, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison()
            .isEqualTo(PreviewBuilder.create()
                .addToCraft(CRAFTING_TABLE, 3)
                .addAvailable(OAK_PLANKS, 8)
                .addMissing(OAK_PLANKS, 4)
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
        final Preview preview = calculatePreview(sut, CRAFTING_TABLE, 1, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison().isEqualTo(PreviewBuilder.create()
            .addToCraft(CRAFTING_TABLE, 1)
            .addToCraft(SPRUCE_PLANKS, 4)
            .addAvailable(SPRUCE_LOG, 1)
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
        final Preview preview = calculatePreview(sut, CRAFTING_TABLE, 11, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison().isEqualTo(PreviewBuilder.create()
            .addToCraft(CRAFTING_TABLE, 11)
            .addAvailable(OAK_PLANKS, 4 * 10)
            .addAvailable(SPRUCE_PLANKS, 4)
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
        final Preview preview = calculatePreview(sut, CRAFTING_TABLE, 16, CancellationToken.NONE);

        // Assert
        assertThat(preview)
            .usingRecursiveComparison()
            .isEqualTo(PreviewBuilder.create()
                .addToCraft(CRAFTING_TABLE, 16)
                .addAvailable(OAK_PLANKS, 4 * 10)
                .addAvailable(SPRUCE_PLANKS, 4 * 5)
                .addMissing(SPRUCE_PLANKS, 4)
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
        final Preview preview = calculatePreview(sut, CRAFTING_TABLE, 1, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison().isEqualTo(PreviewBuilder.create()
            .addToCraft(CRAFTING_TABLE, 1)
            .addToCraft(SPRUCE_PLANKS, 4)
            .addMissing(SPRUCE_LOG, 1)
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
        final Preview preview = calculatePreview(sut, CRAFTING_TABLE, 2, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison().isEqualTo(PreviewBuilder.create()
            .addToCraft(CRAFTING_TABLE, 2)
            .addAvailable(SPRUCE_PLANKS, 8)
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
        final Preview preview = calculatePreview(sut, CRAFTING_TABLE, 3, CancellationToken.NONE);

        // Assert
        assertThat(preview)
            .usingRecursiveComparison()
            .isEqualTo(PreviewBuilder.create()
                .addToCraft(CRAFTING_TABLE, 4)
                .addAvailable(SPRUCE_PLANKS, 8)
                .addMissing(SPRUCE_PLANKS, 8)
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
        final Preview preview = calculatePreview(sut, CRAFTING_TABLE, 3, CancellationToken.NONE);

        // Assert
        assertThat(preview)
            .usingRecursiveComparison()
            .isEqualTo(PreviewBuilder.create()
                .addToCraft(CRAFTING_TABLE, 3)
                .addAvailable(OAK_PLANKS, 3)
                .addToCraft(OAK_PLANKS, 12)
                .addAvailable(OAK_LOG, 3)
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
        final Preview preview = calculatePreview(sut, CRAFTING_TABLE, 3, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison().isEqualTo(PreviewBuilder.create()
            .addToCraft(CRAFTING_TABLE, 3)
            .addToCraft(OAK_PLANKS, 12)
            .addMissing(SPRUCE_LOG, 3)
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
        final Preview preview = calculatePreview(sut, SIGN, 1, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison().isEqualTo(PreviewBuilder.create()
            .addToCraft(SIGN, 3)
            .addToCraft(OAK_PLANKS, 8)
            .addAvailable(OAK_LOG, 4)
            .addToCraft(STICKS, 4)
            .build());
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
        final Preview preview = calculatePreview(sut, SIGN, 1, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison().isEqualTo(PreviewBuilder.create()
            .addToCraft(SIGN, 3)
            .addAvailable(OAK_PLANKS, 6)
            .addToCraft(STICKS, 4)
            .addToCraft(OAK_PLANKS, 2)
            .addAvailable(OAK_LOG, 1)
            .build());
    }

    private static Stream<Arguments> provideMissingResourcesPreview() {
        return Stream.of(
            Arguments.of(1, PreviewBuilder.create()
                .addToCraft(SIGN, 3)
                .addToCraft(OAK_PLANKS, 8)
                .addAvailable(OAK_LOG, 3) // 6
                .addMissing(OAK_LOG, 1) // 2
                .addToCraft(STICKS, 4)
                .build()),
            Arguments.of(4, PreviewBuilder.create()
                .addToCraft(SIGN, 6)
                .addToCraft(OAK_PLANKS, 14)
                .addAvailable(OAK_LOG, 3) // 6
                .addMissing(OAK_LOG, 4) // 4*2=8
                .addToCraft(STICKS, 4)
                .build()),
            Arguments.of(20, PreviewBuilder.create()
                .addToCraft(SIGN, 21) // these are 7 iterations (3 yield per, 7*3=21)
                .addToCraft(OAK_PLANKS, 46) // 4 planks are used for the sticks (4*2=8).
                // that remains 46-4=42
                // 42/7 iterations=6 so checks out
                .addAvailable(OAK_LOG, 3)
                .addMissing(OAK_LOG, 20) // we need 46 planks. we already have 3*2=6 planks!
                // 46-6=40 planks left to craft
                // 40 planks/2 planks per log=20 logs
                .addToCraft(STICKS, 8) // 8*3=21
                .build()
            ));
    }

    @ParameterizedTest
    @MethodSource("provideMissingResourcesPreview")
    void shouldKeepCalculatingEvenIfResourcesAreMissing(
        final long requestedAmount,
        final Preview expectedPreview
    ) {
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
        final Preview preview = calculatePreview(sut, SIGN, requestedAmount, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison().isEqualTo(expectedPreview);
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
        final Preview preview = calculatePreview(sut, OAK_PLANKS, requestedAmount, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison().isEqualTo(PreviewBuilder.create()
            .addToCraft(OAK_PLANKS, planksCrafted)
            .addAvailable(OAK_LOG, logsUsed)
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
        final Preview preview = calculatePreview(sut, OAK_PLANKS, 1, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison().isEqualTo(PreviewBuilder.create()
            .withPatternWithCycle(cycledPattern)
            .build());
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
        final Preview preview = calculatePreview(sut, OAK_PLANKS, 2, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison().isEqualTo(new Preview(
            PreviewType.OVERFLOW, Collections.emptyList(), Collections.emptyList()
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
        final Preview preview = calculatePreview(sut, OAK_PLANKS, Long.MAX_VALUE, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison().isEqualTo(new Preview(
            PreviewType.OVERFLOW, Collections.emptyList(), Collections.emptyList()
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
        final Preview preview = calculatePreview(sut, CRAFTING_TABLE, 2, CancellationToken.NONE);

        // Assert
        assertThat(preview).usingRecursiveComparison().isEqualTo(new Preview(
            PreviewType.OVERFLOW, Collections.emptyList(), Collections.emptyList()
        ));
    }
}
