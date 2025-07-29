package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;

import java.util.Collections;
import java.util.List;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_LOG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class PreviewBuilderTest {
    @Test
    void testDefaultState() {
        // Act
        final Preview preview = PreviewBuilder.create().build();

        // Assert
        assertThat(preview).usingRecursiveComparison()
            .isEqualTo(new Preview(PreviewType.SUCCESS, Collections.emptyList(), Collections.emptyList()));
    }

    @Test
    void testWithPatternWithCycle() {
        // Arrange
        final Pattern pattern = pattern().ingredient(OAK_LOG, 1).output(OAK_PLANKS, 4).build();

        // Act
        final Preview preview = PreviewBuilder.create()
            .withPatternWithCycle(pattern)
            .build();

        // Assert
        assertThat(preview)
            .usingRecursiveComparison()
            .isEqualTo(new Preview(PreviewType.CYCLE_DETECTED, Collections.emptyList(), pattern.layout().outputs()));
    }

    @Test
    void testPreview() {
        // Act
        final Preview preview = PreviewBuilder.create()
            .addToCraft(OAK_PLANKS, 4)
            .addToCraft(OAK_PLANKS, 1)
            .addAvailable(OAK_LOG, 1)
            .addAvailable(OAK_LOG, 2)
            .addMissing(SPRUCE_LOG, 1)
            .addMissing(SPRUCE_LOG, 2)
            .build();

        // Assert
        assertThat(preview).isEqualTo(new Preview(PreviewType.MISSING_RESOURCES, List.of(
            new PreviewItem(OAK_PLANKS, 0, 0, 5),
            new PreviewItem(OAK_LOG, 3, 0, 0),
            new PreviewItem(SPRUCE_LOG, 0, 3, 0)
        ), Collections.emptyList()));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void testToCraftMustBeLargerThanZero(final long amount) {
        // Arrange
        final PreviewBuilder builder = PreviewBuilder.create();

        // Act
        final ThrowableAssert.ThrowingCallable action = () -> builder.addToCraft(OAK_PLANKS, amount);

        // Assert
        assertThatThrownBy(action)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("To craft amount must be larger than 0");
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void testMissingMustBeLargerThanZero(final long amount) {
        // Arrange
        final PreviewBuilder builder = PreviewBuilder.create();

        // Act
        final ThrowableAssert.ThrowingCallable action = () -> builder.addMissing(OAK_PLANKS, amount);

        // Assert
        assertThatThrownBy(action)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Missing amount must be larger than 0");
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void testAvailableMustBeLargerThanZero(final long amount) {
        // Arrange
        final PreviewBuilder builder = PreviewBuilder.create();

        // Act
        final ThrowableAssert.ThrowingCallable action = () -> builder.addAvailable(OAK_PLANKS, amount);

        // Assert
        assertThatThrownBy(action)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Available amount must be larger than 0");
    }
}
