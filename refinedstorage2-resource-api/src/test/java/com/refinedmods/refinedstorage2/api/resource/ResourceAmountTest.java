package com.refinedmods.refinedstorage2.api.resource;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceAmountTest {
    @Test
    void testValidResource() {
        // Act
        final ResourceAmount<String> resourceAmount = new ResourceAmount<>("A", 1);

        // Assert
        assertThat(resourceAmount.getAmount()).isEqualTo(1);
        assertThat(resourceAmount.getResource()).isEqualTo("A");
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testInvalidResource() {
        // Act & assert
        assertThrows(NullPointerException.class, () -> new ResourceAmount<>(null, 1));
    }

    @Test
    void testInvalidAmount() {
        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> new ResourceAmount<>("A", 0));
        assertThrows(IllegalArgumentException.class, () -> new ResourceAmount<>("A", -1));
    }

    @Test
    void shouldNotIncrementZeroOrNegativeAmount() {
        // Arrange
        final ResourceAmount<String> sut = new ResourceAmount<>("A", 1);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.increment(0));
        assertThrows(IllegalArgumentException.class, () -> sut.increment(-1));
    }

    @Test
    void shouldNotDecrementZeroOrNegativeAmount() {
        // Arrange
        final ResourceAmount<String> sut = new ResourceAmount<>("A", 3);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.decrement(0));
        assertThrows(IllegalArgumentException.class, () -> sut.decrement(-1));
    }

    @Test
    void shouldNotDecrementLeadingToZeroAmount() {
        // Arrange
        final ResourceAmount<String> sut = new ResourceAmount<>("A", 3);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.decrement(3));
    }

    @Test
    void shouldIncrement() {
        // Arrange
        final ResourceAmount<String> sut = new ResourceAmount<>("A", 3);

        // Act
        sut.increment(2);

        // Assert
        assertThat(sut.getAmount()).isEqualTo(5);
    }

    @Test
    void shouldDecrement() {
        // Arrange
        final ResourceAmount<String> sut = new ResourceAmount<>("A", 3);

        // Act
        sut.decrement(2);

        // Assert
        assertThat(sut.getAmount()).isEqualTo(1);
    }

    @Test
    void testToString() {
        // Arrange
        final ResourceAmount<String> sut = new ResourceAmount<>("A", 3);

        // Act & assert
        assertThat(sut.toString()).isEqualTo(
            "ResourceAmount{"
                + "resource=A"
                + ", amount=3"
                + '}'
        );
    }
}
