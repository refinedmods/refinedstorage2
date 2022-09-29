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

class CompositeStorageImplExtractTest {
    private CompositeStorageImpl<String> sut;

    @BeforeEach
    void setUp() {
        sut = new CompositeStorageImpl<>(new ResourceListImpl<>());
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

    @Test
    void shouldNotExtractWithoutAnySourcesPresent() {
        // Act
        final long extracted = sut.extract("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
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
}
