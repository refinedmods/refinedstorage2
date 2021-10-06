package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class CompositeStorageTest {
    @Test
    void Test_setting_sources_should_fill_list() {
        // Arrange
        StorageDisk<String> diskStorage1 = new StorageDiskImpl<>(10);
        diskStorage1.insert("A", 10, Action.EXECUTE);

        StorageDisk<String> diskStorage2 = new StorageDiskImpl<>(10);
        diskStorage2.insert("B", 5, Action.EXECUTE);

        StorageDisk<String> diskStorage3 = new StorageDiskImpl<>(10);
        diskStorage3.insert("C", 7, Action.EXECUTE);
        diskStorage3.insert("A", 3, Action.EXECUTE);

        // Act
        CompositeStorage<String> channel = new CompositeStorage<>(Arrays.asList(diskStorage1, diskStorage2, diskStorage3), new ResourceListImpl<>());

        // Assert
        assertThat(channel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 13),
                new ResourceAmount<>("B", 5),
                new ResourceAmount<>("C", 7)
        );
    }

    @Test
    void Test_inserting_without_any_sources_present() {
        // Arrange
        CompositeStorage<String> storage = new CompositeStorage<>(Collections.emptyList(), new ResourceListImpl<>());

        // Act
        long remainder = storage.insert("A", 10, Action.EXECUTE);

        // Assert
        assertThat(remainder).isEqualTo(10);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_insert_without_remainder(Action action) {
        // Arrange
        StorageDisk<String> diskStorage = new StorageDiskImpl<>(20);

        CompositeStorage<String> storage = new CompositeStorage<>(Collections.singletonList(diskStorage), new ResourceListImpl<>());

        // Act
        long remainder = storage.insert("A", 10, action);

        // Assert
        assertThat(remainder).isZero();

        if (action == Action.EXECUTE) {
            assertThat(diskStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(storage.getStored()).isEqualTo(10);
        } else {
            assertThat(diskStorage.getAll()).isEmpty();
            assertThat(storage.getAll()).isEmpty();
            assertThat(storage.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_insert_with_remainder(Action action) {
        // Arrange
        StorageDisk<String> diskStorage = new StorageDiskImpl<>(20);

        CompositeStorage<String> storage = new CompositeStorage<>(Collections.singletonList(diskStorage), new ResourceListImpl<>());

        // Act
        long remainder = storage.insert("A", 30, action);

        // Assert
        assertThat(remainder).isEqualTo(10);

        if (action == Action.EXECUTE) {
            assertThat(diskStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 20)
            );

            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 20)
            );
            assertThat(storage.getStored()).isEqualTo(20);
        } else {
            assertThat(diskStorage.getAll()).isEmpty();

            assertThat(storage.getAll()).isEmpty();
            assertThat(storage.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_insert_without_remainder(Action action) {
        // Arrange
        StorageDisk<String> diskStorage1 = new StorageDiskImpl<>(5);
        StorageDisk<String> diskStorage2 = new StorageDiskImpl<>(10);
        StorageDisk<String> diskStorage3 = new StorageDiskImpl<>(20);

        CompositeStorage<String> storage = new CompositeStorage<>(Arrays.asList(diskStorage1, diskStorage2, diskStorage3), new ResourceListImpl<>());

        // Act
        long remainder = storage.insert("A", 17, action);

        // Assert
        assertThat(remainder).isZero();

        if (action == Action.EXECUTE) {
            assertThat(diskStorage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 5)
            );
            assertThat(diskStorage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(diskStorage3.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 2)
            );

            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 17)
            );
            assertThat(storage.getStored()).isEqualTo(17);
        } else {
            assertThat(diskStorage1.getAll()).isEmpty();
            assertThat(diskStorage2.getAll()).isEmpty();
            assertThat(diskStorage3.getAll()).isEmpty();

            assertThat(storage.getAll()).isEmpty();
            assertThat(storage.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_insert_with_remainder(Action action) {
        // Arrange
        StorageDisk<String> diskStorage1 = new StorageDiskImpl<>(5);
        StorageDisk<String> diskStorage2 = new StorageDiskImpl<>(10);
        StorageDisk<String> diskStorage3 = new StorageDiskImpl<>(20);

        CompositeStorage<String> storage = new CompositeStorage<>(Arrays.asList(diskStorage1, diskStorage2, diskStorage3), new ResourceListImpl<>());

        // Act
        long remainder = storage.insert("A", 39, action);

        // Assert
        assertThat(remainder).isEqualTo(4);

        if (action == Action.EXECUTE) {
            assertThat(diskStorage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 5)
            );
            assertThat(diskStorage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(diskStorage3.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 20)
            );

            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 35)
            );
            assertThat(storage.getStored()).isEqualTo(35);
        } else {
            assertThat(diskStorage1.getAll()).isEmpty();
            assertThat(diskStorage2.getAll()).isEmpty();
            assertThat(diskStorage3.getAll()).isEmpty();

            assertThat(storage.getAll()).isEmpty();
            assertThat(storage.getStored()).isZero();
        }
    }

    @Test
    void Test_extracting_without_any_sources_present() {
        // Arrange
        CompositeStorage<String> storage = new CompositeStorage<>(Collections.emptyList(), new ResourceListImpl<>());

        // Act
        long extracted = storage.extract("A", 10, Action.EXECUTE);

        // Assert
        assertThat(extracted).isZero();
    }

    @Test
    void Test_extracting_without_resource_present() {
        // Arrange
        StorageDisk<String> diskStorage = new StorageDiskImpl<>(10);
        diskStorage.insert("A", 10, Action.EXECUTE);

        CompositeStorage<String> storage = new CompositeStorage<>(Collections.singletonList(diskStorage), new ResourceListImpl<>());

        // Act
        long extracted = storage.extract("B", 10, Action.EXECUTE);

        // Assert
        assertThat(extracted).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_partial_extract(Action action) {
        // Arrange
        StorageDisk<String> diskStorage = new StorageDiskImpl<>(10);
        diskStorage.insert("A", 10, Action.EXECUTE);

        CompositeStorage<String> storage = new CompositeStorage<>(Collections.singletonList(diskStorage), new ResourceListImpl<>());

        // Act
        long extracted = storage.extract("A", 3, action);

        // Assert
        assertThat(extracted).isEqualTo(3);

        if (action == Action.EXECUTE) {
            assertThat(diskStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 7)
            );

            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 7)
            );
            assertThat(storage.getStored()).isEqualTo(7);
        } else {
            assertThat(diskStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );

            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(storage.getStored()).isEqualTo(10);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_full_extract(Action action) {
        // Arrange
        StorageDisk<String> diskStorage = new StorageDiskImpl<>(10);
        diskStorage.insert("A", 10, Action.EXECUTE);

        CompositeStorage<String> storage = new CompositeStorage<>(Collections.singletonList(diskStorage), new ResourceListImpl<>());

        // Act
        long extracted = storage.extract("A", 10, action);

        // Assert
        assertThat(extracted).isEqualTo(10);

        if (action == Action.EXECUTE) {
            assertThat(diskStorage.getAll()).isEmpty();

            assertThat(storage.getAll()).isEmpty();
            assertThat(storage.getStored()).isZero();
        } else {
            assertThat(diskStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );

            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(storage.getStored()).isEqualTo(10);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_more_than_is_available_extract(Action action) {
        // Arrange
        StorageDisk<String> diskStorage = new StorageDiskImpl<>(10);
        diskStorage.insert("A", 4, Action.EXECUTE);

        CompositeStorage<String> storage = new CompositeStorage<>(Collections.singletonList(diskStorage), new ResourceListImpl<>());

        // Act
        long extracted = storage.extract("A", 7, action);

        // Assert
        assertThat(extracted).isEqualTo(4);

        if (action == Action.EXECUTE) {
            assertThat(diskStorage.getAll()).isEmpty();

            assertThat(storage.getAll()).isEmpty();
            assertThat(storage.getStored()).isZero();
        } else {
            assertThat(diskStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 4)
            );

            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 4)
            );
            assertThat(storage.getStored()).isEqualTo(4);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_partial_extract(Action action) {
        // Arrange
        StorageDisk<String> diskStorage1 = new StorageDiskImpl<>(10);
        diskStorage1.insert("A", 10, Action.EXECUTE);

        StorageDisk<String> diskStorage2 = new StorageDiskImpl<>(5);
        diskStorage2.insert("A", 3, Action.EXECUTE);

        CompositeStorage<String> storage = new CompositeStorage<>(Arrays.asList(diskStorage1, diskStorage2), new ResourceListImpl<>());

        // Act
        long extracted = storage.extract("A", 12, action);

        // Assert
        assertThat(extracted).isEqualTo(12);

        if (action == Action.EXECUTE) {
            assertThat(diskStorage1.getAll()).isEmpty();
            assertThat(diskStorage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 1)
            );

            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 1)
            );
            assertThat(storage.getStored()).isEqualTo(1);
        } else {
            assertThat(diskStorage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(diskStorage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 3)
            );

            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 13)
            );
            assertThat(storage.getStored()).isEqualTo(13);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_full_extract(Action action) {
        // Arrange
        StorageDisk<String> diskStorage1 = new StorageDiskImpl<>(10);
        diskStorage1.insert("A", 10, Action.EXECUTE);

        StorageDisk<String> diskStorage2 = new StorageDiskImpl<>(5);
        diskStorage2.insert("A", 3, Action.EXECUTE);

        CompositeStorage<String> storage = new CompositeStorage<>(Arrays.asList(diskStorage1, diskStorage2), new ResourceListImpl<>());

        // Act
        long extracted = storage.extract("A", 13, action);

        // Assert
        assertThat(extracted).isEqualTo(13);

        if (action == Action.EXECUTE) {
            assertThat(diskStorage1.getAll()).isEmpty();
            assertThat(diskStorage2.getAll()).isEmpty();

            assertThat(storage.getAll()).isEmpty();
            assertThat(storage.getStored()).isZero();
        } else {
            assertThat(diskStorage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(diskStorage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 3)
            );

            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 13)
            );
            assertThat(storage.getStored()).isEqualTo(13);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_more_than_is_available_extract(Action action) {
        // Arrange
        StorageDisk<String> diskStorage1 = new StorageDiskImpl<>(10);
        diskStorage1.insert("A", 10, Action.EXECUTE);

        StorageDisk<String> diskStorage2 = new StorageDiskImpl<>(5);
        diskStorage2.insert("A", 3, Action.EXECUTE);

        CompositeStorage<String> storage = new CompositeStorage<>(Arrays.asList(diskStorage1, diskStorage2), new ResourceListImpl<>());

        // Act
        long extracted = storage.extract("A", 30, action);

        // Assert
        assertThat(extracted).isEqualTo(13);

        if (action == Action.EXECUTE) {
            assertThat(diskStorage1.getAll()).isEmpty();
            assertThat(diskStorage2.getAll()).isEmpty();

            assertThat(storage.getAll()).isEmpty();
            assertThat(storage.getStored()).isZero();
        } else {
            assertThat(diskStorage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(diskStorage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 3)
            );

            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 13)
            );
            assertThat(storage.getStored()).isEqualTo(13);
        }
    }

    @Test
    void Test_prioritizing_when_inserting() {
        // Arrange
        PrioritizedStorage<String> highestPriority = new PrioritizedStorage<>(10, new StorageDiskImpl<>(10));
        PrioritizedStorage<String> lowestPriority = new PrioritizedStorage<>(5, new StorageDiskImpl<>(10));

        // Act
        CompositeStorage<String> channel = new CompositeStorage<>(Arrays.asList(lowestPriority, highestPriority), new ResourceListImpl<>());

        channel.insert("A", 11, Action.EXECUTE);

        // Assert
        assertThat(highestPriority.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 10)
        );
        assertThat(lowestPriority.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 1)
        );
    }

    @Test
    void Test_prioritizing_when_extracting() {
        // Arrange
        PrioritizedStorage<String> highestPriority = new PrioritizedStorage<>(10, new StorageDiskImpl<>(10));
        PrioritizedStorage<String> lowestPriority = new PrioritizedStorage<>(5, new StorageDiskImpl<>(10));

        highestPriority.insert("A", 10, Action.EXECUTE);
        lowestPriority.insert("A", 5, Action.EXECUTE);

        // Act
        CompositeStorage<String> channel = new CompositeStorage<>(Arrays.asList(lowestPriority, highestPriority), new ResourceListImpl<>());

        channel.extract("A", 11, Action.EXECUTE);

        // Assert
        assertThat(highestPriority.getAll()).isEmpty();
        assertThat(lowestPriority.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 4)
        );
    }
}
