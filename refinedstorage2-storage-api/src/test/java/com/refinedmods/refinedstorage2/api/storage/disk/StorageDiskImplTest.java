package com.refinedmods.refinedstorage2.api.storage.disk;

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
class StorageDiskImplTest {
    private final StorageDisk<String> disk = new StorageDiskImpl<>(100);

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_adding_a_resource(Action action) {
        // Act
        long remainder = disk.insert("A", 64, action);

        // Assert
        assertThat(remainder).isZero();

        if (action == Action.EXECUTE) {
            assertThat(disk.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 64)
            );
            assertThat(disk.getStored()).isEqualTo(64);
        } else {
            assertThat(disk.getAll()).isEmpty();
            assertThat(disk.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_adding_a_resource_and_exceeding_capacity(Action action) {
        // Act
        long remainder1 = disk.insert("A", 60, Action.EXECUTE);
        long remainder2 = disk.insert("B", 45, action);

        // Assert
        assertThat(remainder1).isZero();
        assertThat(remainder2).isEqualTo(5);

        if (action == Action.EXECUTE) {
            assertThat(disk.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                    new ResourceAmount<>("A", 60),
                    new ResourceAmount<>("B", 40)
            );
            assertThat(disk.getStored()).isEqualTo(100);
        } else {
            assertThat(disk.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 60)
            );
            assertThat(disk.getStored()).isEqualTo(60);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_adding_resource_to_an_already_full_disk_and_exceeding_capacity(Action action) {
        // Act
        long remainder1 = disk.insert("A", 100, Action.EXECUTE);
        long remainder2 = disk.insert("A", 101, action);

        // Assert
        assertThat(remainder1).isZero();
        assertThat(remainder2).isEqualTo(101);

        assertThat(disk.getStored()).isEqualTo(100);
    }

    @Test
    void Test_adding_resource_with_negative_capacity_storage() {
        // Arrange
        StorageDisk<String> diskStorage = new StorageDiskImpl<>(-1);

        // Act
        long remainder = diskStorage.insert("A", Integer.MAX_VALUE, Action.EXECUTE);

        // Assert
        assertThat(remainder).isZero();
        assertThat(diskStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", Integer.MAX_VALUE)
        );
    }

    @Test
    void Test_adding_resource_with_zero_capacity_storage() {
        // Arrange
        StorageDisk<String> diskStorage = new StorageDiskImpl<>(0);

        // Act
        long remainder = diskStorage.insert("A", 1, Action.EXECUTE);

        // Assert
        assertThat(remainder).isEqualTo(1);
    }

    @Test
    void Test_adding_invalid_resource() {
        // Act
        Executable action1 = () -> disk.insert("A", 0, Action.EXECUTE);
        Executable action2 = () -> disk.insert("A", -1, Action.EXECUTE);
        Executable action3 = () -> disk.insert(null, 1, Action.EXECUTE);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
        assertThrows(IllegalArgumentException.class, action3);
    }

    @Test
    void Test_extracting_non_existent_resource() {
        // Act
        long extracted = disk.extract("A", 1, Action.EXECUTE);

        // Assert
        assertThat(extracted).isZero();
        assertThat(disk.getStored()).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_resource_partly(Action action) {
        // Arrange
        disk.insert("A", 32, Action.EXECUTE);

        // Act
        long extracted = disk.extract("A", 2, action);

        // Assert
        assertThat(extracted).isEqualTo(2);

        if (action == Action.EXECUTE) {
            assertThat(disk.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 30)
            );
            assertThat(disk.getStored()).isEqualTo(30);
        } else {
            assertThat(disk.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 32)
            );
            assertThat(disk.getStored()).isEqualTo(32);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_resource_completely(Action action) {
        // Arrange
        disk.insert("A", 32, Action.EXECUTE);

        // Act
        long extracted = disk.extract("A", 32, action);

        // Assert
        assertThat(extracted).isEqualTo(32);

        if (action == Action.EXECUTE) {
            assertThat(disk.getAll()).isEmpty();
            assertThat(disk.getStored()).isZero();
        } else {
            assertThat(disk.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 32)
            );
            assertThat(disk.getStored()).isEqualTo(32);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_resource_more_than_is_available(Action action) {
        // Arrange
        disk.insert("A", 32, Action.EXECUTE);

        // Act
        long extracted = disk.extract("A", 33, action);

        // Assert
        assertThat(extracted).isEqualTo(32);

        if (action == Action.EXECUTE) {
            assertThat(disk.getAll()).isEmpty();
            assertThat(disk.getStored()).isZero();
        } else {
            assertThat(disk.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 32)
            );
            assertThat(disk.getStored()).isEqualTo(32);
        }
    }

    @Test
    void Test_extracting_invalid_resource_count() {
        // Act
        Executable action1 = () -> disk.extract("A", 0, Action.EXECUTE);
        Executable action2 = () -> disk.extract("A", -1, Action.EXECUTE);
        Executable action3 = () -> disk.extract(null, 1, Action.EXECUTE);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
        assertThrows(IllegalArgumentException.class, action3);
    }
}
