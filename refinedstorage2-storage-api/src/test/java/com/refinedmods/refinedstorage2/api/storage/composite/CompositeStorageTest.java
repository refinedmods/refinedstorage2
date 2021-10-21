package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
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
        Storage<String> storage1 = new CappedStorage<>(10);
        storage1.insert("A", 10, Action.EXECUTE);

        Storage<String> storage2 = new CappedStorage<>(10);
        storage2.insert("B", 5, Action.EXECUTE);

        Storage<String> storage3 = new CappedStorage<>(10);
        storage3.insert("C", 7, Action.EXECUTE);
        storage3.insert("A", 3, Action.EXECUTE);

        // Act
        CompositeStorage<String> sut = new CompositeStorage<>(Arrays.asList(storage1, storage2, storage3), new ResourceListImpl<>());

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 13),
                new ResourceAmount<>("B", 5),
                new ResourceAmount<>("C", 7)
        );
    }

    @Test
    void Test_inserting_without_any_sources_present() {
        // Arrange
        CompositeStorage<String> sut = new CompositeStorage<>(Collections.emptyList(), new ResourceListImpl<>());

        // Act
        long remainder = sut.insert("A", 10, Action.EXECUTE);

        // Assert
        assertThat(remainder).isEqualTo(10);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_insert_without_remainder(Action action) {
        // Arrange
        Storage<String> storage = new CappedStorage<>(20);

        CompositeStorage<String> sut = new CompositeStorage<>(Collections.singletonList(storage), new ResourceListImpl<>());

        // Act
        long remainder = sut.insert("A", 10, action);

        // Assert
        assertThat(remainder).isZero();

        if (action == Action.EXECUTE) {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(sut.getStored()).isEqualTo(10);
        } else {
            assertThat(storage.getAll()).isEmpty();
            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_insert_with_remainder(Action action) {
        // Arrange
        Storage<String> storage = new CappedStorage<>(20);

        CompositeStorage<String> sut = new CompositeStorage<>(Collections.singletonList(storage), new ResourceListImpl<>());

        // Act
        long remainder = sut.insert("A", 30, action);

        // Assert
        assertThat(remainder).isEqualTo(10);

        if (action == Action.EXECUTE) {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 20)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 20)
            );
            assertThat(sut.getStored()).isEqualTo(20);
        } else {
            assertThat(storage.getAll()).isEmpty();

            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_insert_without_remainder(Action action) {
        // Arrange
        Storage<String> storage1 = new CappedStorage<>(5);
        Storage<String> storage2 = new CappedStorage<>(10);
        Storage<String> storage3 = new CappedStorage<>(20);

        CompositeStorage<String> sut = new CompositeStorage<>(Arrays.asList(storage1, storage2, storage3), new ResourceListImpl<>());

        // Act
        long remainder = sut.insert("A", 17, action);

        // Assert
        assertThat(remainder).isZero();

        if (action == Action.EXECUTE) {
            assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 5)
            );
            assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(storage3.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 2)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 17)
            );
            assertThat(sut.getStored()).isEqualTo(17);
        } else {
            assertThat(storage1.getAll()).isEmpty();
            assertThat(storage2.getAll()).isEmpty();
            assertThat(storage3.getAll()).isEmpty();

            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_insert_with_remainder(Action action) {
        // Arrange
        Storage<String> storage1 = new CappedStorage<>(5);
        Storage<String> storage2 = new CappedStorage<>(10);
        Storage<String> storage3 = new CappedStorage<>(20);

        CompositeStorage<String> sut = new CompositeStorage<>(Arrays.asList(storage1, storage2, storage3), new ResourceListImpl<>());

        // Act
        long remainder = sut.insert("A", 39, action);

        // Assert
        assertThat(remainder).isEqualTo(4);

        if (action == Action.EXECUTE) {
            assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 5)
            );
            assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(storage3.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 20)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 35)
            );
            assertThat(sut.getStored()).isEqualTo(35);
        } else {
            assertThat(storage1.getAll()).isEmpty();
            assertThat(storage2.getAll()).isEmpty();
            assertThat(storage3.getAll()).isEmpty();

            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        }
    }

    @Test
    void Test_extracting_without_any_sources_present() {
        // Arrange
        CompositeStorage<String> sut = new CompositeStorage<>(Collections.emptyList(), new ResourceListImpl<>());

        // Act
        long extracted = sut.extract("A", 10, Action.EXECUTE);

        // Assert
        assertThat(extracted).isZero();
    }

    @Test
    void Test_extracting_without_resource_present() {
        // Arrange
        Storage<String> storage = new CappedStorage<>(10);
        storage.insert("A", 10, Action.EXECUTE);

        CompositeStorage<String> sut = new CompositeStorage<>(Collections.singletonList(storage), new ResourceListImpl<>());

        // Act
        long extracted = sut.extract("B", 10, Action.EXECUTE);

        // Assert
        assertThat(extracted).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_partial_extract(Action action) {
        // Arrange
        Storage<String> storage = new CappedStorage<>(10);
        storage.insert("A", 10, Action.EXECUTE);

        CompositeStorage<String> sut = new CompositeStorage<>(Collections.singletonList(storage), new ResourceListImpl<>());

        // Act
        long extracted = sut.extract("A", 3, action);

        // Assert
        assertThat(extracted).isEqualTo(3);

        if (action == Action.EXECUTE) {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 7)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 7)
            );
            assertThat(sut.getStored()).isEqualTo(7);
        } else {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(sut.getStored()).isEqualTo(10);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_full_extract(Action action) {
        // Arrange
        Storage<String> storage = new CappedStorage<>(10);
        storage.insert("A", 10, Action.EXECUTE);

        CompositeStorage<String> sut = new CompositeStorage<>(Collections.singletonList(storage), new ResourceListImpl<>());

        // Act
        long extracted = sut.extract("A", 10, action);

        // Assert
        assertThat(extracted).isEqualTo(10);

        if (action == Action.EXECUTE) {
            assertThat(storage.getAll()).isEmpty();

            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        } else {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(sut.getStored()).isEqualTo(10);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_more_than_is_available_extract(Action action) {
        // Arrange
        Storage<String> storage = new CappedStorage<>(10);
        storage.insert("A", 4, Action.EXECUTE);

        CompositeStorage<String> sut = new CompositeStorage<>(Collections.singletonList(storage), new ResourceListImpl<>());

        // Act
        long extracted = sut.extract("A", 7, action);

        // Assert
        assertThat(extracted).isEqualTo(4);

        if (action == Action.EXECUTE) {
            assertThat(storage.getAll()).isEmpty();

            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        } else {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 4)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 4)
            );
            assertThat(sut.getStored()).isEqualTo(4);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_partial_extract(Action action) {
        // Arrange
        Storage<String> storage1 = new CappedStorage<>(10);
        storage1.insert("A", 10, Action.EXECUTE);

        Storage<String> storage2 = new CappedStorage<>(5);
        storage2.insert("A", 3, Action.EXECUTE);

        CompositeStorage<String> sut = new CompositeStorage<>(Arrays.asList(storage1, storage2), new ResourceListImpl<>());

        // Act
        long extracted = sut.extract("A", 12, action);

        // Assert
        assertThat(extracted).isEqualTo(12);

        if (action == Action.EXECUTE) {
            assertThat(storage1.getAll()).isEmpty();
            assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 1)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 1)
            );
            assertThat(sut.getStored()).isEqualTo(1);
        } else {
            assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 3)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 13)
            );
            assertThat(sut.getStored()).isEqualTo(13);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_full_extract(Action action) {
        // Arrange
        Storage<String> storage1 = new CappedStorage<>(10);
        storage1.insert("A", 10, Action.EXECUTE);

        Storage<String> storage2 = new CappedStorage<>(5);
        storage2.insert("A", 3, Action.EXECUTE);

        CompositeStorage<String> sut = new CompositeStorage<>(Arrays.asList(storage1, storage2), new ResourceListImpl<>());

        // Act
        long extracted = sut.extract("A", 13, action);

        // Assert
        assertThat(extracted).isEqualTo(13);

        if (action == Action.EXECUTE) {
            assertThat(storage1.getAll()).isEmpty();
            assertThat(storage2.getAll()).isEmpty();

            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        } else {
            assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 3)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 13)
            );
            assertThat(sut.getStored()).isEqualTo(13);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_more_than_is_available_extract(Action action) {
        // Arrange
        Storage<String> storage1 = new CappedStorage<>(10);
        storage1.insert("A", 10, Action.EXECUTE);

        Storage<String> storage2 = new CappedStorage<>(5);
        storage2.insert("A", 3, Action.EXECUTE);

        CompositeStorage<String> sut = new CompositeStorage<>(Arrays.asList(storage1, storage2), new ResourceListImpl<>());

        // Act
        long extracted = sut.extract("A", 30, action);

        // Assert
        assertThat(extracted).isEqualTo(13);

        if (action == Action.EXECUTE) {
            assertThat(storage1.getAll()).isEmpty();
            assertThat(storage2.getAll()).isEmpty();

            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        } else {
            assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
            assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 3)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 13)
            );
            assertThat(sut.getStored()).isEqualTo(13);
        }
    }

    @Test
    void Test_prioritizing_when_inserting() {
        // Arrange
        PrioritizedStorage<String> highestPriority = new PrioritizedStorage<>(10, new CappedStorage<>(10));
        PrioritizedStorage<String> lowestPriority = new PrioritizedStorage<>(5, new CappedStorage<>(10));

        // Act
        CompositeStorage<String> sut = new CompositeStorage<>(Arrays.asList(lowestPriority, highestPriority), new ResourceListImpl<>());

        sut.insert("A", 11, Action.EXECUTE);

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
        PrioritizedStorage<String> highestPriority = new PrioritizedStorage<>(10, new CappedStorage<>(10));
        PrioritizedStorage<String> lowestPriority = new PrioritizedStorage<>(5, new CappedStorage<>(10));

        highestPriority.insert("A", 10, Action.EXECUTE);
        lowestPriority.insert("A", 5, Action.EXECUTE);

        // Act
        CompositeStorage<String> sut = new CompositeStorage<>(Arrays.asList(lowestPriority, highestPriority), new ResourceListImpl<>());

        sut.extract("A", 11, Action.EXECUTE);

        // Assert
        assertThat(highestPriority.getAll()).isEmpty();
        assertThat(lowestPriority.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 4)
        );
    }
}
