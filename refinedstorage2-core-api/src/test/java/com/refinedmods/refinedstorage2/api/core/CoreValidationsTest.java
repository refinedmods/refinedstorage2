package com.refinedmods.refinedstorage2.api.core;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoreValidationsTest {
    @Test
    void shouldValidateEquals() {
        // Act & assert
        final Exception e1 = assertThrows(
            IllegalStateException.class,
            () -> CoreValidations.validateEquals(1, 2, "bla")
        );
        assertThat(e1.getMessage()).isEqualTo("bla");

        final Exception e2 = assertThrows(
            IllegalStateException.class,
            () -> CoreValidations.validateEquals(null, 2, "bla")
        );
        assertThat(e2.getMessage()).isEqualTo("bla");

        final Exception e3 = assertThrows(
            IllegalStateException.class,
            () -> CoreValidations.validateEquals(1, null, "bla")
        );
        assertThat(e3.getMessage()).isEqualTo("bla");

        assertDoesNotThrow(() -> CoreValidations.validateEquals(1, 1, "bla"));
        assertDoesNotThrow(() -> CoreValidations.validateEquals(null, null, "bla"));
    }

    @Test
    void shouldValidateNotNull() {
        // Act & assert
        final Exception e = assertThrows(
            NullPointerException.class,
            () -> CoreValidations.validateNotNull(null, "bla")
        );
        assertThat(e.getMessage()).isEqualTo("bla");

        assertDoesNotThrow(() -> CoreValidations.validateNotNull("not null", "bla"));

        assertThat(CoreValidations.validateNotNull("not null", "bla")).isEqualTo("not null");
    }

    @Test
    void shouldValidateNotNegative() {
        // Act & assert
        final Exception e = assertThrows(
            IllegalArgumentException.class,
            () -> CoreValidations.validateNotNegative(-1, "bla")
        );
        assertThat(e.getMessage()).isEqualTo("bla");

        assertDoesNotThrow(() -> CoreValidations.validateNotNegative(0, "bla"));
        assertDoesNotThrow(() -> CoreValidations.validateNotNegative(1, "bla"));

        assertThat(CoreValidations.validateNotNegative(0, "test")).isZero();
    }

    @Test
    void shouldValidateNegative() {
        // Act & assert
        final Exception e1 = assertThrows(
            IllegalArgumentException.class,
            () -> CoreValidations.validateNegative(0, "bla")
        );
        assertThat(e1.getMessage()).isEqualTo("bla");

        final Exception e2 = assertThrows(
            IllegalArgumentException.class,
            () -> CoreValidations.validateNegative(1, "bla")
        );
        assertThat(e2.getMessage()).isEqualTo("bla");

        assertDoesNotThrow(() -> CoreValidations.validateNegative(-1, "bla"));
        assertDoesNotThrow(() -> CoreValidations.validateNegative(-2, "bla"));
    }

    @Test
    void shouldValidateLargerThanZero() {
        // Act & assert
        final Exception e = assertThrows(
            IllegalArgumentException.class,
            () -> CoreValidations.validateLargerThanZero(0, "bla")
        );
        assertThat(e.getMessage()).isEqualTo("bla");

        final Exception e2 =
            assertThrows(IllegalArgumentException.class, () -> CoreValidations.validateLargerThanZero(-1, "bla"));
        assertThat(e2.getMessage()).isEqualTo("bla");

        assertDoesNotThrow(() -> CoreValidations.validateLargerThanZero(1, "bla"));
        assertDoesNotThrow(() -> CoreValidations.validateLargerThanZero(2, "bla"));
    }

    @Test
    void shouldValidateEmpty() {
        // Act & assert
        final List<Integer> badList = List.of(1);
        final Exception e = assertThrows(
            IllegalArgumentException.class,
            () -> CoreValidations.validateEmpty(badList, "bla")
        );
        assertThat(e.getMessage()).isEqualTo("bla");

        assertDoesNotThrow(() -> CoreValidations.validateEmpty(List.of(), "bla"));
    }

    @Test
    void shouldValidateContains() {
        // Act & assert
        final List<Integer> badList = List.of(1);
        final Exception e = assertThrows(
            IllegalArgumentException.class,
            () -> CoreValidations.validateContains(badList, 2, "bla")
        );
        assertThat(e.getMessage()).isEqualTo("bla");

        assertDoesNotThrow(() -> CoreValidations.validateContains(List.of(1), 1, "bla"));
    }

    @Test
    void shouldValidateNotContains() {
        // Act & assert
        final List<Integer> badList = List.of(1);
        final Exception e = assertThrows(
            IllegalArgumentException.class,
            () -> CoreValidations.validateNotContains(badList, 1, "bla")
        );
        assertThat(e.getMessage()).isEqualTo("bla");

        assertDoesNotThrow(() -> CoreValidations.validateNotContains(List.of(1), 2, "bla"));
    }
}
