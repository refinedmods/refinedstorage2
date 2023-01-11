package com.refinedmods.refinedstorage2.api.storage.external;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalStorageTest {
    SpyingExternalStorageListener listener;

    @BeforeEach
    void setUp() {
        listener = new SpyingExternalStorageListener();
    }

    @Test
    void shouldNotTakeExistingResourcesIntoConsiderationWhenBuildingInitialState() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        final Storage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage), listener);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldTakeExistingResourcesIntoConsiderationWhenDetectingChanges() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage), listener);

        // Act
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A!", 10)
        );
        assertThat(sut.getStored()).isEqualTo(10);
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldDetectCompletelyNewResource() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage), listener);

        // Act
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A!", 10)
        );
        assertThat(sut.getStored()).isEqualTo(10);
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldDetectAdditionToExistingResource() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage), listener);

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
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }


    @Test
    void shouldDetectPartialRemovalOfExistingResource() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage), listener);

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
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldDetectCompleteRemovalOfExistingResource() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage), listener);

        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.detectChanges();

        // Act
        storage.extract("A!", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldDetectCompleteRemovalOfExistingResourceAndAdditionOfNewResource() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage), listener);

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
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldDetectAdditionOfExistingResourceAndAdditionOfNewResource() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage), listener);

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
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldNotDetectAnyChangesWhenNoChangesAreMade() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage), listener);

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
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsertAndDetectChanges(final Action action) {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final Storage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage), listener);

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
            assertThat(listener.resources).containsExactly("A", "A", "B");
            assertThat(listener.actors).containsOnly(EmptyActor.INSTANCE);
        } else {
            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
            assertThat(listener.resources).isEmpty();
            assertThat(listener.actors).isEmpty();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotCallListenerWhenInsertionFailed(final Action action) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(0);
        final Storage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage), listener);

        // Act
        final long extracted = sut.insert("A", 1, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldPartiallyExtractAndDetectChanges(final Action action) {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final Storage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage), listener);
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
            assertThat(listener.resources).containsExactly("A", "A2", "B", "A!");
            assertThat(listener.actors).containsOnly(EmptyActor.INSTANCE);
        } else {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A!", 10),
                new ResourceAmount<>("A2!", 10),
                new ResourceAmount<>("B!", 10)
            );
            assertThat(sut.getStored()).isEqualTo(30);
            assertThat(listener.resources).containsExactly("A", "A2", "B");
            assertThat(listener.actors).containsOnly(EmptyActor.INSTANCE);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldCompletelyExtractAndDetectChanges(final Action action) {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final Storage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage), listener);
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
            assertThat(listener.resources).containsExactly("A", "A2", "B", "A!");
            assertThat(listener.actors).containsOnly(EmptyActor.INSTANCE);
        } else {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A!", 10),
                new ResourceAmount<>("A2!", 10),
                new ResourceAmount<>("B!", 10)
            );
            assertThat(sut.getStored()).isEqualTo(30);
            assertThat(listener.resources).containsExactly("A", "A2", "B");
            assertThat(listener.actors).containsOnly(EmptyActor.INSTANCE);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotCallListenerWhenExtractionFailed(final Action action) {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final Storage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage), listener);

        // Act
        final long extracted = sut.extract("A", 10, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }
}
