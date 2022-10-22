package com.refinedmods.refinedstorage2.api.storage.external;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalStorageTest {
    @Test
    void shouldNotTakeExistingResourcesIntoConsiderationWhenBuildingInitialState() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        final Storage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
    }

    @Test
    void shouldTakeExistingResourcesIntoConsiderationWhenDetectingChanges() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));

        // Act
        sut.detectChanges();

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A!", 10)
        );
        assertThat(sut.getStored()).isEqualTo(10);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsertAndDetectChanges(final Action action) {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final Storage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));

        // Act
        final long insertedA1 = sut.insert("A", 10, action, EmptyActor.INSTANCE);
        final long insertedA2 = sut.insert("A", 1, action, EmptyActor.INSTANCE);
        final long insertedB = sut.insert("B", 5, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(insertedA1).isEqualTo(10);
        assertThat(insertedA2).isEqualTo(1);
        assertThat(insertedB).isEqualTo(5);

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A!", 11),
                new ResourceAmount<>("B!", 5)
            );
            assertThat(sut.getStored()).isEqualTo(16);
        } else {
            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldPartiallyExtractAndDetectChanges(final Action action) {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final Storage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));
        sut.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.insert("A2", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.insert("B", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        // this will try to extract A!(5) and A2!(5/2)
        final long extracted = sut.extract("A!", 5, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(5);

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A!", 5),
                new ResourceAmount<>("A2!", 8),
                new ResourceAmount<>("B!", 10)
            );
            assertThat(sut.getStored()).isEqualTo(23);
        } else {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A!", 10),
                new ResourceAmount<>("A2!", 10),
                new ResourceAmount<>("B!", 10)
            );
            assertThat(sut.getStored()).isEqualTo(30);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldCompletelyExtractAndDetectChanges(final Action action) {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final Storage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));
        sut.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.insert("A2", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.insert("B", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        // this will try to extract A!(10) and A2!(10/2)
        final long extracted = sut.extract("A!", 10, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(10);

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A2!", 5),
                new ResourceAmount<>("B!", 10)
            );
            assertThat(sut.getStored()).isEqualTo(15);
        } else {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A!", 10),
                new ResourceAmount<>("A2!", 10),
                new ResourceAmount<>("B!", 10)
            );
            assertThat(sut.getStored()).isEqualTo(30);
        }
    }
}
