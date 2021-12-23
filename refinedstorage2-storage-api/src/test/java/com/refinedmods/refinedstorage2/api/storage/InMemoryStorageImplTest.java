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
class InMemoryStorageImplTest {
    private final Storage<String> sut = new InMemoryStorageImpl<>();

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_adding_a_resource(Action action) {
        // Act
        long remainder = sut.insert("A", 64, action);

        // Assert
        assertThat(remainder).isZero();

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 64)
            );
            assertThat(sut.getStored()).isEqualTo(64);
        } else {
            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        }
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

    @Test
    void Test_extracting_non_existent_resource() {
        // Act
        long extracted = sut.extract("A", 1, Action.EXECUTE);

        // Assert
        assertThat(extracted).isZero();
        assertThat(sut.getStored()).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_resource_partly(Action action) {
        // Arrange
        sut.insert("A", 32, Action.EXECUTE);

        // Act
        long extracted = sut.extract("A", 2, action);

        // Assert
        assertThat(extracted).isEqualTo(2);

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 30)
            );
            assertThat(sut.getStored()).isEqualTo(30);
        } else {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 32)
            );
            assertThat(sut.getStored()).isEqualTo(32);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_resource_completely(Action action) {
        // Arrange
        sut.insert("A", 32, Action.EXECUTE);

        // Act
        long extracted = sut.extract("A", 32, action);

        // Assert
        assertThat(extracted).isEqualTo(32);

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        } else {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 32)
            );
            assertThat(sut.getStored()).isEqualTo(32);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_resource_more_than_is_available(Action action) {
        // Arrange
        sut.insert("A", 32, Action.EXECUTE);

        // Act
        long extracted = sut.extract("A", 33, action);

        // Assert
        assertThat(extracted).isEqualTo(32);

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        } else {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 32)
            );
            assertThat(sut.getStored()).isEqualTo(32);
        }
    }

    @Test
    void Test_extracting_invalid_resource_count() {
        // Act
        Executable action1 = () -> sut.extract("A", 0, Action.EXECUTE);
        Executable action2 = () -> sut.extract("A", -1, Action.EXECUTE);
        Executable action3 = () -> sut.extract(null, 1, Action.EXECUTE);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
        assertThrows(NullPointerException.class, action3);
    }
}
