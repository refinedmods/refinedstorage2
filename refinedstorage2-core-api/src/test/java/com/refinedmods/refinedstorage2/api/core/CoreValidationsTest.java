package com.refinedmods.refinedstorage2.api.core;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CoreValidationsTest {
    @Test
    void shouldValidateNotNull() {
        // Act & assert
        Exception e = assertThrows(NullPointerException.class, () -> CoreValidations.validateNotNull(null, "bla"));
        assertThat(e.getMessage()).isEqualTo("bla");

        assertDoesNotThrow(() -> CoreValidations.validateNotNull("not null", "bla"));
    }

    @Test
    void shouldValidateNonNegative() {
        // Act & assert
        Exception e =
            assertThrows(IllegalArgumentException.class, () -> CoreValidations.validateNonNegative(-1, "bla"));
        assertThat(e.getMessage()).isEqualTo("bla");

        assertDoesNotThrow(() -> CoreValidations.validateNonNegative(0, "bla"));
        assertDoesNotThrow(() -> CoreValidations.validateNonNegative(1, "bla"));
    }

    @Test
    void shouldValidateLargerThanZero() {
        // Act & assert
        Exception e =
            assertThrows(IllegalArgumentException.class, () -> CoreValidations.validateLargerThanZero(0, "bla"));
        assertThat(e.getMessage()).isEqualTo("bla");

        Exception e2 =
            assertThrows(IllegalArgumentException.class, () -> CoreValidations.validateLargerThanZero(-1, "bla"));
        assertThat(e2.getMessage()).isEqualTo("bla");

        assertDoesNotThrow(() -> CoreValidations.validateLargerThanZero(1, "bla"));
        assertDoesNotThrow(() -> CoreValidations.validateLargerThanZero(2, "bla"));
    }

    @Test
    void shouldValidateEmpty() {
        // Act & assert
        Exception e =
            assertThrows(IllegalArgumentException.class, () -> CoreValidations.validateEmpty(List.of(1), "bla"));
        assertThat(e.getMessage()).isEqualTo("bla");

        assertDoesNotThrow(() -> CoreValidations.validateEmpty(List.of(), "bla"));
    }

    @Test
    void shouldValidateContains() {
        // Act & assert
        Exception e =
            assertThrows(IllegalArgumentException.class, () -> CoreValidations.validateContains(List.of(1), 2, "bla"));
        assertThat(e.getMessage()).isEqualTo("bla");

        assertDoesNotThrow(() -> CoreValidations.validateContains(List.of(1), 1, "bla"));
    }
}