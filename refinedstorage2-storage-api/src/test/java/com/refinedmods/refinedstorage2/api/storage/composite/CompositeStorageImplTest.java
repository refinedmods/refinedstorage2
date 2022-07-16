package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ActorCapturingStorage;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.FakeSources;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class CompositeStorageImplTest {
    private CompositeStorageImpl<String> sut;

    @BeforeEach
    void setUp() {
        sut = new CompositeStorageImpl<>(new ResourceListImpl<>());
    }

    @Test
    void testInitialState() {
        // Act & assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
    }

    @Test
    void shouldAddSource() {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(10);
        storage1.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> storage2 = new LimitedStorageImpl<>(10);
        storage2.insert("B", 5, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> storage3 = new LimitedStorageImpl<>(10);
        storage3.insert("C", 7, Action.EXECUTE, EmptyActor.INSTANCE);
        storage3.insert("A", 3, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        final long inserted = sut.insert("B", 6, Action.SIMULATE, EmptyActor.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 13),
            new ResourceAmount<>("B", 5),
            new ResourceAmount<>("C", 7)
        );
        assertThat(inserted).isEqualTo(5);
    }

    @Test
    void shouldRespectPriorityWhenAddingNewSources() {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(10);
        final Storage<String> storage2 = new LimitedStorageImpl<>(10);
        final Storage<String> storage3 = new LimitedStorageImpl<>(10);

        // Act
        sut.addSource(new PrioritizedStorage<>(20, storage1));
        sut.addSource(new PrioritizedStorage<>(10, storage2));
        sut.addSource(new PrioritizedStorage<>(30, storage3));

        final long inserted = sut.insert("A", 12, Action.EXECUTE, EmptyActor.INSTANCE);

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
    void shouldRespectPriorityWhenRemovingSources() {
        // Arrange
        final Storage<String> storage1 = new PrioritizedStorage<>(20, new LimitedStorageImpl<>(10));
        final Storage<String> storage2 = new PrioritizedStorage<>(10, new LimitedStorageImpl<>(10));
        final Storage<String> storage3 = new PrioritizedStorage<>(30, new LimitedStorageImpl<>(10));

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);
        sut.removeSource(storage3);

        // Act
        final long inserted = sut.insert("A", 12, Action.EXECUTE, EmptyActor.INSTANCE);

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
    void shouldRemoveSource() {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(10);
        storage1.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> storage2 = new LimitedStorageImpl<>(10);
        storage2.insert("B", 5, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> storage3 = new LimitedStorageImpl<>(10);
        storage3.insert("C", 7, Action.EXECUTE, EmptyActor.INSTANCE);
        storage3.insert("A", 3, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        // Act
        sut.removeSource(storage3);

        final long extracted = sut.extract("C", 1, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 10),
            new ResourceAmount<>("B", 5)
        );
        assertThat(extracted).isZero();
    }

    @Test
    void shouldClearSources() {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(10);
        storage1.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> storage2 = new LimitedStorageImpl<>(10);
        storage2.insert("B", 5, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> storage3 = new LimitedStorageImpl<>(10);
        storage3.insert("C", 7, Action.EXECUTE, EmptyActor.INSTANCE);
        storage3.insert("A", 3, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        // Act
        sut.clearSources();

        final long extracted = sut.extract("C", 1, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(extracted).isZero();
    }

    @Test
    void shouldNotInsertWithoutAnySourcesPresent() {
        // Act
        final long inserted = sut.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsertToSingleSourceWithoutRemainder(final Action action) {
        // Arrange
        final ActorCapturingStorage<String> storage = new ActorCapturingStorage<>(new LimitedStorageImpl<>(20));
        sut.addSource(storage);

        final Actor actor = () -> "Custom";

        // Act
        final long inserted = sut.insert("A", 10, action, actor);

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

        assertThat(storage.getActors()).containsExactly(actor);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsertToSingleSourceWithRemainder(final Action action) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(20);
        sut.addSource(storage);

        // Act
        final long inserted = sut.insert("A", 30, action, EmptyActor.INSTANCE);

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
    void shouldInsertToMultipleSourcesWithoutRemainder(final Action action) {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(5);
        final Storage<String> storage2 = new LimitedStorageImpl<>(10);
        final Storage<String> storage3 = new LimitedStorageImpl<>(20);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        // Act
        final long inserted = sut.insert("A", 17, action, EmptyActor.INSTANCE);

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
    void shouldInsertToMultipleSourcesWithRemainder(final Action action) {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(5);
        final Storage<String> storage2 = new LimitedStorageImpl<>(10);
        final Storage<String> storage3 = new LimitedStorageImpl<>(20);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        // Act
        final long inserted = sut.insert("A", 39, action, EmptyActor.INSTANCE);

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
    void shouldNotExtractWithoutAnySourcesPresent() {
        // Act
        final long extracted = sut.extract("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
    }

    @Test
    void shouldNotExtractWithoutResourcePresent() {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(storage);

        // Act
        final long extracted = sut.extract("B", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractFromSingleSourcePartially(final Action action) {
        // Arrange
        final ActorCapturingStorage<String> storage = new ActorCapturingStorage<>(new LimitedStorageImpl<>(10));
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(storage);

        final Actor actor = () -> "Custom";

        // Act
        final long extracted = sut.extract("A", 3, action, actor);

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

        assertThat(storage.getActors()).containsExactly(EmptyActor.INSTANCE, actor);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractFromSingleSourceCompletely(final Action action) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(storage);

        // Act
        final long extracted = sut.extract("A", 10, action, EmptyActor.INSTANCE);

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
    void shouldNotExtractMoreThanIsAvailableFromSingleSource(final Action action) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 4, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(storage);

        // Act
        final long extracted = sut.extract("A", 7, action, EmptyActor.INSTANCE);

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
    void shouldExtractFromMultipleSourcesPartially(final Action action) {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(10);
        storage1.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> storage2 = new LimitedStorageImpl<>(5);
        storage2.insert("A", 3, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(storage1);
        sut.addSource(storage2);

        // Act
        final long extracted = sut.extract("A", 12, action, EmptyActor.INSTANCE);

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
    void shouldExtractFromMultipleSourcesCompletely(final Action action) {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(10);
        storage1.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> storage2 = new LimitedStorageImpl<>(5);
        storage2.insert("A", 3, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(storage1);
        sut.addSource(storage2);

        // Act
        final long extracted = sut.extract("A", 13, action, EmptyActor.INSTANCE);

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
    void shouldNotExtractMoreThanIsAvailableFromMultipleSources(final Action action) {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(10);
        storage1.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> storage2 = new LimitedStorageImpl<>(5);
        storage2.insert("A", 3, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(storage1);
        sut.addSource(storage2);

        // Act
        final long extracted = sut.extract("A", 30, action, EmptyActor.INSTANCE);

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
    void shouldRespectPriorityWhenInserting() {
        // Arrange
        final PrioritizedStorage<String> lowestPriority = new PrioritizedStorage<>(5, new LimitedStorageImpl<>(10));
        final PrioritizedStorage<String> highestPriority = new PrioritizedStorage<>(10, new LimitedStorageImpl<>(10));

        sut.addSource(lowestPriority);
        sut.addSource(highestPriority);

        // Act
        sut.insert("A", 11, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(highestPriority.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 10)
        );
        assertThat(lowestPriority.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 1)
        );
    }

    @Test
    void shouldRespectPriorityWhenExtracting() {
        // Arrange
        final PrioritizedStorage<String> lowestPriority = new PrioritizedStorage<>(5, new LimitedStorageImpl<>(10));
        final PrioritizedStorage<String> highestPriority = new PrioritizedStorage<>(10, new LimitedStorageImpl<>(10));

        lowestPriority.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);
        highestPriority.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(lowestPriority);
        sut.addSource(highestPriority);

        // Act
        sut.extract("A", 11, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(highestPriority.getAll()).isEmpty();
        assertThat(lowestPriority.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 4)
        );
    }

    @Test
    void testAddingCompositeStorageSource() {
        // Arrange
        final CompositeStorage<String> subComposite = new CompositeStorageImpl<>(new ResourceListImpl<>());
        subComposite.addSource(new InMemoryStorageImpl<>());
        subComposite.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        sut.addSource(subComposite);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 10)
        );
    }

    @Test
    void testRemovingCompositeStorageSource() {
        // Arrange
        final CompositeStorage<String> subComposite = new CompositeStorageImpl<>(new ResourceListImpl<>());
        subComposite.addSource(new InMemoryStorageImpl<>());
        subComposite.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(subComposite);

        final Storage<String> subCompositeStorage = new InMemoryStorageImpl<>();
        subCompositeStorage.insert("B", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        sut.removeSource(subComposite);

        subComposite.addSource(subCompositeStorage);

        // Assert
        assertThat(sut.getAll()).isEmpty();
    }

    @Test
    void shouldFindMostRecentChange() {
        // Arrange
        final AtomicLong clock = new AtomicLong(0L);

        final TrackedStorage<String> a = new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), clock::get);
        final TrackedStorage<String> b = new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), clock::get);

        // Test if it uses the latest across 2 different storages
        a.insert("1", 1, Action.EXECUTE, FakeSources.FakeActor1.INSTANCE);
        clock.set(1L);
        b.insert("1", 1, Action.EXECUTE, FakeSources.FakeActor1.INSTANCE);

        // Test if it differentiates between source types properly
        clock.set(2L);
        b.insert("2", 1, Action.EXECUTE, FakeSources.FakeActor1.INSTANCE);
        clock.set(3L);
        b.insert("2", 1, Action.EXECUTE, FakeSources.FakeActor2.INSTANCE);

        sut.addSource(a);
        sut.addSource(b);

        // Act
        final var oneOne = sut.findTrackedResourceByActorType("1", FakeSources.FakeActor1.class);
        final var oneTwo = sut.findTrackedResourceByActorType("1", FakeSources.FakeActor2.class);
        final var twoOne = sut.findTrackedResourceByActorType("2", FakeSources.FakeActor1.class);
        final var twoTwo = sut.findTrackedResourceByActorType("2", FakeSources.FakeActor2.class);

        // Assert
        assertThat(oneOne).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 1L));
        assertThat(oneTwo).isEmpty();
        assertThat(twoOne).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 2L));
        assertThat(twoTwo).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source2", 3L));
    }
}
