package com.refinedmods.refinedstorage2.api.stack;

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
    void Test_valid_resource_amount() {
        // Act
        ResourceAmount<String> resourceAmount = new ResourceAmount<>("A", 1);

        // Assert
        assertThat(resourceAmount.getAmount()).isEqualTo(1);
        assertThat(resourceAmount.getResource()).isEqualTo("A");
    }
}
