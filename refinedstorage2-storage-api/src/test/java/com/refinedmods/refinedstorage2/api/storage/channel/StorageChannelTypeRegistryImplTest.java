package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class StorageChannelTypeRegistryImplTest {
    StorageChannelTypeRegistryImpl sut;

    @BeforeEach
    void setUp() {
        sut = new StorageChannelTypeRegistryImpl();
    }

    @Test
    void Test_adding_duplicate_type() {
        // Arrange
        StorageChannelType<String> a = StorageChannelImpl::new;
        sut.addType(a);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.addType(a));
    }

    @Test
    void Test_adding_type() {
        // Arrange
        StorageChannelType<String> a = StorageChannelImpl::new;
        StorageChannelType<String> b = StorageChannelImpl::new;

        // Act
        sut.addType(b);
        sut.addType(a);

        // Assert
        assertThat(sut.getTypes()).containsExactlyInAnyOrder(a, b);
    }
}
