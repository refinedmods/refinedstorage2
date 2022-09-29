package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ActorCapturingStorage;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class CompositeStorageImplInsertTest {
    private CompositeStorageImpl<String> sut;

    @BeforeEach
    void setUp() {
        sut = new CompositeStorageImpl<>(new ResourceListImpl<>());
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
    void shouldNotInsertWithoutAnySourcesPresent() {
        // Act
        final long inserted = sut.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isZero();
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
}
