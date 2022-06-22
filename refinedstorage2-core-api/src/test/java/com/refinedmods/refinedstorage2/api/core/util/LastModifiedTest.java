package com.refinedmods.refinedstorage2.api.core.util;

import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class LastModifiedTest {
    @ParameterizedTest
    @ValueSource(longs = {0, 1000, 59 * 1000, (60 * 1000) - 1})
    void Test_modified_seconds(long timePassed) {
        // Act
        LastModified lastModified = LastModified.calculate(0, timePassed);

        // Assert
        assertThat(lastModified).usingRecursiveComparison().isEqualTo(new LastModified(LastModified.Type.SECOND, timePassed / 1000));
    }

    @ParameterizedTest
    @ValueSource(longs = {60 * 1000, 60 * 59 * 1000, (60 * 60 * 1000) - 1})
    void Test_modified_minutes(long timePassed) {
        // Act
        LastModified lastModified = LastModified.calculate(0, timePassed);

        // Assert
        assertThat(lastModified).usingRecursiveComparison().isEqualTo(new LastModified(LastModified.Type.MINUTE, timePassed / (60 * 1000)));
    }

    @ParameterizedTest
    @ValueSource(longs = {60 * 60 * 1000, 60 * 60 * 23 * 1000, (60 * 60 * 24 * 1000) - 1})
    void Test_modified_hours(long timePassed) {
        // Act
        LastModified lastModified = LastModified.calculate(0, timePassed);

        // Assert
        assertThat(lastModified).usingRecursiveComparison().isEqualTo(new LastModified(LastModified.Type.HOUR, timePassed / (60 * 60 * 1000)));
    }

    @ParameterizedTest
    @ValueSource(longs = {60 * 60 * 24 * 1000, 60 * 60 * 24 * 6 * 1000, (60 * 60 * 24 * 7 * 1000) - 1})
    void Test_modified_days(long timePassed) {
        // Act
        LastModified lastModified = LastModified.calculate(0, timePassed);

        // Assert
        assertThat(lastModified).usingRecursiveComparison().isEqualTo(new LastModified(LastModified.Type.DAY, timePassed / (60 * 60 * 24 * 1000)));
    }

    @ParameterizedTest
    @ValueSource(longs = {60 * 60 * 24 * 7 * 1000, 60L * 60 * 24 * 364 * 1000, (60L * 60 * 24 * 365 * 1000) - 1})
    void Test_modified_weeks(long timePassed) {
        // Act
        LastModified lastModified = LastModified.calculate(0, timePassed);

        // Assert
        assertThat(lastModified).usingRecursiveComparison().isEqualTo(new LastModified(LastModified.Type.WEEK, timePassed / (60 * 60 * 24 * 7 * 1000)));
    }

    @ParameterizedTest
    @ValueSource(longs = {60L * 60 * 24 * 365 * 1000, 60L * 60 * 24 * 365 * 10 * 1000})
    void Test_modified_years(long timePassed) {
        // Act
        LastModified lastModified = LastModified.calculate(0, timePassed);

        // Assert
        assertThat(lastModified).usingRecursiveComparison().isEqualTo(new LastModified(LastModified.Type.YEAR, timePassed / (60L * 60 * 24 * 365 * 1000)));
    }
}
