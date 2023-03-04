package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LastModifiedTest {
    @ParameterizedTest
    @ValueSource(longs = {0, 1000, 59 * 1000, (60 * 1000) - 1})
    void shouldHandleSecondsAgo(final long timePassed) {
        // Act
        final LastModified lastModified = LastModified.calculate(
            System.currentTimeMillis(),
            System.currentTimeMillis() + timePassed
        );

        // Assert
        assertThat(lastModified).usingRecursiveComparison()
            .isEqualTo(new LastModified(LastModified.Type.SECOND, timePassed / 1000));
    }

    @ParameterizedTest
    @ValueSource(longs = {60 * 1000, 60 * 59 * 1000, (60 * 60 * 1000) - 1})
    void shouldHandleMinutesAgo(final long timePassed) {
        // Act
        final LastModified lastModified = LastModified.calculate(
            System.currentTimeMillis(),
            System.currentTimeMillis() + timePassed
        );

        // Assert
        assertThat(lastModified).usingRecursiveComparison()
            .isEqualTo(new LastModified(LastModified.Type.MINUTE, timePassed / (60 * 1000)));
    }

    @ParameterizedTest
    @ValueSource(longs = {60 * 60 * 1000, 60 * 60 * 23 * 1000, (60 * 60 * 24 * 1000) - 1})
    void shouldHandleHoursAgo(final long timePassed) {
        // Act
        final LastModified lastModified = LastModified.calculate(
            System.currentTimeMillis(),
            System.currentTimeMillis() + timePassed
        );

        // Assert
        assertThat(lastModified).usingRecursiveComparison()
            .isEqualTo(new LastModified(LastModified.Type.HOUR, timePassed / (60 * 60 * 1000)));
    }

    @ParameterizedTest
    @ValueSource(longs = {60 * 60 * 24 * 1000, 60 * 60 * 24 * 6 * 1000, (60 * 60 * 24 * 7 * 1000) - 1})
    void shouldHandleDaysAgo(final long timePassed) {
        // Act
        final LastModified lastModified = LastModified.calculate(
            System.currentTimeMillis(),
            System.currentTimeMillis() + timePassed
        );

        // Assert
        assertThat(lastModified).usingRecursiveComparison()
            .isEqualTo(new LastModified(LastModified.Type.DAY, timePassed / (60 * 60 * 24 * 1000)));
    }

    @ParameterizedTest
    @ValueSource(longs = {60 * 60 * 24 * 7 * 1000, 60L * 60 * 24 * 364 * 1000, (60L * 60 * 24 * 365 * 1000) - 1})
    void shouldHandleWeeksAgo(final long timePassed) {
        // Act
        final LastModified lastModified = LastModified.calculate(
            System.currentTimeMillis(),
            System.currentTimeMillis() + timePassed
        );

        // Assert
        assertThat(lastModified).usingRecursiveComparison()
            .isEqualTo(new LastModified(LastModified.Type.WEEK, timePassed / (60 * 60 * 24 * 7 * 1000)));
    }

    @ParameterizedTest
    @ValueSource(longs = {60L * 60 * 24 * 365 * 1000, 60L * 60 * 24 * 365 * 10 * 1000})
    void shouldHandleYearsAgo(final long timePassed) {
        // Act
        final LastModified lastModified = LastModified.calculate(
            System.currentTimeMillis(),
            System.currentTimeMillis() + timePassed
        );

        // Assert
        assertThat(lastModified).usingRecursiveComparison()
            .isEqualTo(new LastModified(LastModified.Type.YEAR, timePassed / (60L * 60 * 24 * 365 * 1000)));
    }
}
