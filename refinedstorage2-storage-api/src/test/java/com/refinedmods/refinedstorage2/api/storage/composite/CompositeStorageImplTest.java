package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.SourceCapturingStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class CompositeStorageImplTest {
    private CompositeStorageImpl<String> sut;

    @BeforeEach
    void setUp() {
        sut = new CompositeStorageImpl<>(new ResourceListImpl<>());
    }

    @Test
    void Test_initial_state() {
        // Act & assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
    }

    @Test
    void Test_adding_sources() {
        // Arrange
        Storage<String> storage1 = new CappedStorage<>(10);
        storage1.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        Storage<String> storage2 = new CappedStorage<>(10);
        storage2.insert("B", 5, Action.EXECUTE, EmptySource.INSTANCE);

        Storage<String> storage3 = new CappedStorage<>(10);
        storage3.insert("C", 7, Action.EXECUTE, EmptySource.INSTANCE);
        storage3.insert("A", 3, Action.EXECUTE, EmptySource.INSTANCE);

        // Act
        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        long inserted = sut.insert("B", 6, Action.SIMULATE, EmptySource.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 13),
                new ResourceAmount<>("B", 5),
                new ResourceAmount<>("C", 7)
        );
        assertThat(inserted).isEqualTo(5);
    }

    @Test
    void Test_priority_sorting_when_adding_source() {
        // Arrange
        Storage<String> storage1 = new CappedStorage<>(10);
        Storage<String> storage2 = new CappedStorage<>(10);
        Storage<String> storage3 = new CappedStorage<>(10);

        sut.addSource(new PrioritizedStorage<>(20, storage1));
        sut.addSource(new PrioritizedStorage<>(10, storage2));
        sut.addSource(new PrioritizedStorage<>(30, storage3));

        // Act
        long inserted = sut.insert("A", 12, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 12)
        );
        assertThat(inserted).isEqualTo(12);
        assertThat(storage3.getStored()).isEqualTo(10);
        assertThat(storage1.getStored()).isEqualTo(2);
        assertThat(storage2.getStored()).isZero();
    }

    @Test
    void Test_priority_sorting_when_removing_source() {
        // Arrange
        Storage<String> storage1 = new PrioritizedStorage<>(20, new CappedStorage<>(10));
        Storage<String> storage2 = new PrioritizedStorage<>(10, new CappedStorage<>(10));
        Storage<String> storage3 = new PrioritizedStorage<>(30, new CappedStorage<>(10));

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);
        sut.removeSource(storage3);

        // Act
        long inserted = sut.insert("A", 12, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 12)
        );
        assertThat(inserted).isEqualTo(12);
        assertThat(storage1.getStored()).isEqualTo(10);
        assertThat(storage2.getStored()).isEqualTo(2);
        assertThat(storage3.getStored()).isZero();
    }

    @Test
    void Test_removing_sources() {
        // Arrange
        Storage<String> storage1 = new CappedStorage<>(10);
        storage1.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        Storage<String> storage2 = new CappedStorage<>(10);
        storage2.insert("B", 5, Action.EXECUTE, EmptySource.INSTANCE);

        Storage<String> storage3 = new CappedStorage<>(10);
        storage3.insert("C", 7, Action.EXECUTE, EmptySource.INSTANCE);
        storage3.insert("A", 3, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        // Act
        sut.removeSource(storage3);

        long extracted = sut.extract("C", 1, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 10),
                new ResourceAmount<>("B", 5)
        );
        assertThat(extracted).isZero();
    }

    @Test
    void Test_clearing_sources() {
        // Arrange
        Storage<String> storage1 = new CappedStorage<>(10);
        storage1.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        Storage<String> storage2 = new CappedStorage<>(10);
        storage2.insert("B", 5, Action.EXECUTE, EmptySource.INSTANCE);

        Storage<String> storage3 = new CappedStorage<>(10);
        storage3.insert("C", 7, Action.EXECUTE, EmptySource.INSTANCE);
        storage3.insert("A", 3, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        // Act
        sut.clearSources();

        long extracted = sut.extract("C", 1, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(extracted).isZero();
    }

    @Test
    void Test_inserting_without_any_sources_present() {
        // Act
        long inserted = sut.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(inserted).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_insert_without_remainder(Action action) {
        // Arrange
        SourceCapturingStorage<String> storage = new SourceCapturingStorage<>(new CappedStorage<>(20));
        sut.addSource(storage);

        Source customSource = () -> "Custom";

        // Act
        long inserted = sut.insert("A", 10, action, customSource);

        // Assert
        assertThat(inserted).isEqualTo(10);

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

        assertThat(storage.getSourcesUsed()).containsExactly(customSource);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_insert_with_remainder(Action action) {
        // Arrange
        Storage<String> storage = new CappedStorage<>(20);
        sut.addSource(storage);

        // Act
        long inserted = sut.insert("A", 30, action, EmptySource.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(20);

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

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        // Act
        long inserted = sut.insert("A", 17, action, EmptySource.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(17);

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

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        // Act
        long inserted = sut.insert("A", 39, action, EmptySource.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(35);

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
        // Act
        long extracted = sut.extract("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
    }

    @Test
    void Test_extracting_without_resource_present() {
        // Arrange
        Storage<String> storage = new CappedStorage<>(10);
        storage.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(storage);

        // Act
        long extracted = sut.extract("B", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_partial_extract(Action action) {
        // Arrange
        SourceCapturingStorage<String> storage = new SourceCapturingStorage<>(new CappedStorage<>(10));
        storage.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(storage);

        Source customSource = () -> "Custom";

        // Act
        long extracted = sut.extract("A", 3, action, customSource);

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

        assertThat(storage.getSourcesUsed()).containsExactly(EmptySource.INSTANCE, customSource);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_full_extract(Action action) {
        // Arrange
        Storage<String> storage = new CappedStorage<>(10);
        storage.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(storage);

        // Act
        long extracted = sut.extract("A", 10, action, EmptySource.INSTANCE);

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
        storage.insert("A", 4, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(storage);

        // Act
        long extracted = sut.extract("A", 7, action, EmptySource.INSTANCE);

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
        storage1.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        Storage<String> storage2 = new CappedStorage<>(5);
        storage2.insert("A", 3, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(storage1);
        sut.addSource(storage2);

        // Act
        long extracted = sut.extract("A", 12, action, EmptySource.INSTANCE);

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
        storage1.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        Storage<String> storage2 = new CappedStorage<>(5);
        storage2.insert("A", 3, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(storage1);
        sut.addSource(storage2);

        // Act
        long extracted = sut.extract("A", 13, action, EmptySource.INSTANCE);

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
        storage1.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        Storage<String> storage2 = new CappedStorage<>(5);
        storage2.insert("A", 3, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(storage1);
        sut.addSource(storage2);

        // Act
        long extracted = sut.extract("A", 30, action, EmptySource.INSTANCE);

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
        PrioritizedStorage<String> lowestPriority = new PrioritizedStorage<>(5, new CappedStorage<>(10));
        PrioritizedStorage<String> highestPriority = new PrioritizedStorage<>(10, new CappedStorage<>(10));

        sut.addSource(lowestPriority);
        sut.addSource(highestPriority);

        // Act
        sut.insert("A", 11, Action.EXECUTE, EmptySource.INSTANCE);

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
        PrioritizedStorage<String> lowestPriority = new PrioritizedStorage<>(5, new CappedStorage<>(10));
        PrioritizedStorage<String> highestPriority = new PrioritizedStorage<>(10, new CappedStorage<>(10));

        lowestPriority.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);
        highestPriority.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(lowestPriority);
        sut.addSource(highestPriority);

        // Act
        sut.extract("A", 11, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(highestPriority.getAll()).isEmpty();
        assertThat(lowestPriority.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 4)
        );
    }

    @Test
    void Test_adding_composite_source() {
        // Arrange
        CompositeStorage<String> subComposite = new CompositeStorageImpl<>(new ResourceListImpl<>());
        subComposite.addSource(new InMemoryStorageImpl<>());
        subComposite.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Act
        sut.addSource(subComposite);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 10)
        );
    }

    @Test
    void Test_removing_composite_source() {
        // Arrange
        CompositeStorage<String> subComposite = new CompositeStorageImpl<>(new ResourceListImpl<>());
        subComposite.addSource(new InMemoryStorageImpl<>());
        subComposite.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(subComposite);

        Storage<String> subCompositeStorage = new InMemoryStorageImpl<>();
        subCompositeStorage.insert("B", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Act
        sut.removeSource(subComposite);

        subComposite.addSource(subCompositeStorage);

        // Assert
        assertThat(sut.getAll()).isEmpty();
    }

    @Test
    void Test_adding_source_to_sub_composite_should_notify_parent() {
        // Arrange
        CompositeStorage<String> subComposite = new CompositeStorageImpl<>(new ResourceListImpl<>());
        Storage<String> subStorage = new InMemoryStorageImpl<>();
        subStorage.insert("B", 10, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(subComposite);
        sut.addSource(subStorage);

        Storage<String> subCompositeStorage = new InMemoryStorageImpl<>();
        subCompositeStorage.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Act
        subComposite.addSource(subCompositeStorage);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 10),
                new ResourceAmount<>("B", 10)
        );

        assertThat(subComposite.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 10)
        );
    }

    @Test
    void Test_removing_source_from_sub_composite_should_notify_parent() {
        // Arrange
        CompositeStorage<String> subComposite = new CompositeStorageImpl<>(new ResourceListImpl<>());
        Storage<String> subStorage = new InMemoryStorageImpl<>();
        subStorage.insert("B", 10, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(subComposite);
        sut.addSource(subStorage);

        Storage<String> subCompositeStorage = new InMemoryStorageImpl<>();
        subCompositeStorage.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        subComposite.addSource(subCompositeStorage);

        // Act
        subComposite.removeSource(subCompositeStorage);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("B", 10)
        );
        assertThat(subComposite.getAll()).isEmpty();
    }

    @Test
    void Test_should_find_most_recent_change() {
        // Arrange
        AtomicLong clock = new AtomicLong(0L);

        TrackedStorage<String> a = new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), clock::get);
        TrackedStorage<String> b = new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), clock::get);

        // Test if it uses the latest across 2 different storages
        a.insert("1", 1, Action.EXECUTE, CustomSource1.INSTANCE);
        clock.set(1L);
        b.insert("1", 1, Action.EXECUTE, CustomSource1.INSTANCE);

        // Test if it differentiates between source types properly
        clock.set(2L);
        b.insert("2", 1, Action.EXECUTE, CustomSource1.INSTANCE);
        clock.set(3L);
        b.insert("2", 1, Action.EXECUTE, CustomSource2.INSTANCE);

        sut.addSource(a);
        sut.addSource(b);

        // Act
        var oneOne = sut.findTrackedResourceBySourceType("1", CustomSource1.class);
        var oneTwo = sut.findTrackedResourceBySourceType("1", CustomSource2.class);

        var twoOne = sut.findTrackedResourceBySourceType("2", CustomSource1.class);
        var twoTwo = sut.findTrackedResourceBySourceType("2", CustomSource2.class);

        // Assert
        assertThat(oneOne).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Custom1", 1L));
        assertThat(oneTwo).isEmpty();
        assertThat(twoOne).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Custom1", 2L));
        assertThat(twoTwo).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Custom2", 3L));
    }

    private static class CustomSource1 implements Source {
        private static final Source INSTANCE = new CustomSource1();

        @Override
        public String getName() {
            return "Custom1";
        }
    }

    private static class CustomSource2 implements Source {
        private static final Source INSTANCE = new CustomSource2();

        @Override
        public String getName() {
            return "Custom2";
        }
    }
}
