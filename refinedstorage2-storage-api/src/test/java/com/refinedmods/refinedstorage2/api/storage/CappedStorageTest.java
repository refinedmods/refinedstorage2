package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class CappedStorageTest {
    private final Storage<String> sut = new CappedStorage<>(100);

    @Test
    void Test_negative_capacity() {
        // Arrange
        Storage<String> backed = new InMemoryStorageImpl<>();

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> new CappedStorage<>(backed, -1));
    }

    @Test
    void Test_zero_capacity() {
        // Arrange
        Storage<String> backed = new InMemoryStorageImpl<>();
        Storage<String> sut = new CappedStorage<>(backed, 0);

        // Act
        long remainder = sut.insert("A", 1, Action.EXECUTE);

        // Assert
        assertThat(remainder).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_adding_a_resource(Action action) {
        // Act
        long remainder = sut.insert("A", 100, action);

        // Assert
        assertThat(remainder).isZero();

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                    new ResourceAmount<>("A", 100)
            );
            assertThat(sut.getStored()).isEqualTo(100);
        } else {
            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_adding_a_resource_and_exceeding_capacity(Action action) {
        // Act
        long remainder1 = sut.insert("A", 60, Action.EXECUTE);
        long remainder2 = sut.insert("B", 45, action);

        // Assert
        assertThat(remainder1).isZero();
        assertThat(remainder2).isEqualTo(5);

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                    new ResourceAmount<>("A", 60),
                    new ResourceAmount<>("B", 40)
            );
            assertThat(sut.getStored()).isEqualTo(100);
        } else {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 60)
            );
            assertThat(sut.getStored()).isEqualTo(60);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_adding_resource_to_an_already_full_storage_and_exceeding_capacity(Action action) {
        // Act
        long remainder1 = sut.insert("A", 100, Action.EXECUTE);
        long remainder2 = sut.insert("A", 101, action);

        // Assert
        assertThat(remainder1).isZero();
        assertThat(remainder2).isEqualTo(101);

        assertThat(sut.getStored()).isEqualTo(100);
    }

    @Test
    void Test_adding_invalid_resource() {
        // Act
        Executable action1 = () -> sut.insert("A", 0, Action.EXECUTE);
        Executable action2 = () -> sut.insert("A", -1, Action.EXECUTE);
        Executable action3 = () -> sut.insert(null, 1, Action.EXECUTE);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
        assertThrows(NullPointerException.class, action3);
    }
}
