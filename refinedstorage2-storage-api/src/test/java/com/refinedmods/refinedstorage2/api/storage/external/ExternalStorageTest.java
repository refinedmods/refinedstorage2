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
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A!", 10)
        );
        assertThat(sut.getStored()).isEqualTo(10);
    }

    @Test
    void shouldDetectCompletelyNewResource() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));

        // Act
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A!", 10)
        );
        assertThat(sut.getStored()).isEqualTo(10);
    }

    @Test
    void shouldDetectAdditionToExistingResource() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));

        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.detectChanges();

        // Act
        storage.insert("A", 1, Action.EXECUTE, EmptyActor.INSTANCE);
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A!", 11)
        );
        assertThat(sut.getStored()).isEqualTo(11);
    }


    @Test
    void shouldDetectPartialRemovalOfExistingResource() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));

        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.detectChanges();

        // Act
        storage.extract("A!", 2, Action.EXECUTE, EmptyActor.INSTANCE);
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A!", 8)
        );
        assertThat(sut.getStored()).isEqualTo(8);
    }

    @Test
    void shouldDetectCompleteRemovalOfExistingResource() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));

        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.detectChanges();

        // Act
        storage.extract("A!", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
    }

    @Test
    void shouldDetectCompleteRemovalOfExistingResourceAndAdditionOfNewResource() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));

        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.detectChanges();

        // Act
        storage.extract("A!", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert("B", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B!", 10)
        );
        assertThat(sut.getStored()).isEqualTo(10);
    }

    @Test
    void shouldDetectAdditionOfExistingResourceAndAdditionOfNewResource() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));

        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.detectChanges();

        // Act
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert("B", 1, Action.EXECUTE, EmptyActor.INSTANCE);
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A!", 20),
            new ResourceAmount<>("B!", 1)
        );
        assertThat(sut.getStored()).isEqualTo(21);
    }

    @Test
    void shouldNotDetectAnyChangesWhenNoChangesAreMade() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));

        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert("B", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.extract("A!", 5, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.extract("B!", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.detectChanges();

        // Act
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isFalse();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A!", 15)
        );
        assertThat(sut.getStored()).isEqualTo(15);
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
