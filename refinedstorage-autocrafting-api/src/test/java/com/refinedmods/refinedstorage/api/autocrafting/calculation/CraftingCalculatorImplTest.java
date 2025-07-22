package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingUtil.patterns;
import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingUtil.storage;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.CRAFTING_TABLE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_PLANKS;
import static org.assertj.core.api.Assertions.assertThat;

class CraftingCalculatorImplTest {
    @Test
    void shouldNotFindMaxAmountIfThereAreAlwaysMissingResources() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_PLANKS, 1)
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
        final long maxAmount = sut.getMaxAmount(CRAFTING_TABLE, CancellationToken.NONE);

        // Assert
        assertThat(maxAmount).isZero();
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 64L, 128L})
    void shouldFindMaxAmount(final long amountPossible) {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, amountPossible)
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
        final long maxAmount = sut.getMaxAmount(CRAFTING_TABLE, CancellationToken.NONE);

        // Assert
        assertThat(maxAmount).isEqualTo(amountPossible);
    }

    @Test
    void shouldNotFindMaxAmountIfThereIsANumberOverflow() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_PLANKS, Long.MAX_VALUE)
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
        final long maxAmount = sut.getMaxAmount(CRAFTING_TABLE, CancellationToken.NONE);

        // Assert
        assertThat(maxAmount).isZero();
    }
}
