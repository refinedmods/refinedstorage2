package com.refinedmods.refinedstorage2.api.resource;

import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class ResourceAmountTest {
    @Test
    void Test_invalid_resource() {
        // Act & assert
        assertThrows(NullPointerException.class, () -> new ResourceAmount<>(null, 1));
    }

    @Test
    void Test_invalid_amount() {
        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> new ResourceAmount<>("A", 0));
        assertThrows(IllegalArgumentException.class, () -> new ResourceAmount<>("A", -1));
    }

    @Test
    void Test_invalid_increment_amount() {
        // Arrange
        ResourceAmount<String> sut = new ResourceAmount<>("A", 1);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.increment(0));
        assertThrows(IllegalArgumentException.class, () -> sut.increment(-1));
    }

    @Test
    void Test_invalid_decrement_amount() {
        // Arrange
        ResourceAmount<String> sut = new ResourceAmount<>("A", 3);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.decrement(3));
        assertThrows(IllegalArgumentException.class, () -> sut.decrement(0));
        assertThrows(IllegalArgumentException.class, () -> sut.decrement(-1));
    }

    @Test
    void Test_incrementing() {
        // Arrange
        ResourceAmount<String> sut = new ResourceAmount<>("A", 3);

        // Act
        sut.increment(2);

        // Assert
        assertThat(sut.getAmount()).isEqualTo(5);
    }

    @Test
    void Test_decrementing() {
        // Arrange
        ResourceAmount<String> sut = new ResourceAmount<>("A", 3);

        // Act
        sut.decrement(2);

        // Assert
        assertThat(sut.getAmount()).isEqualTo(1);
    }

    @Test
    void Test_valid_resource_amount() {
        // Act
        ResourceAmount<String> resourceAmount = new ResourceAmount<>("A", 1);

        // Assert
        assertThat(resourceAmount.getAmount()).isEqualTo(1);
        assertThat(resourceAmount.getResource()).isEqualTo("A");
    }
}
