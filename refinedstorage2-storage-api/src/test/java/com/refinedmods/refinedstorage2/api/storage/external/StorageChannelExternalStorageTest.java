package com.refinedmods.refinedstorage2.api.storage.external;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class StorageChannelExternalStorageTest {
    @Test
    void shouldNotTakeExistingResourcesIntoConsiderationWhenBuildingInitialState() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final Storage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));
        final StorageChannel<String> storageChannel = new StorageChannelImpl<>();

        // Act
        storageChannel.addSource(sut);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
        assertThat(storageChannel.getAll()).isEmpty();
        assertThat(storageChannel.getStored()).isZero();
    }

    @Test
    void shouldTakeExistingResourcesIntoConsiderationWhenDetectingChanges() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final ExternalStorage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));
        final StorageChannel<String> storageChannel = new StorageChannelImpl<>();
        storageChannel.addSource(sut);

        // Act
        sut.detectChanges();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A!", 10)
        );
        assertThat(storageChannel.getStored()).isEqualTo(10);
    }

    @Test
    void shouldNoLongerPropagateChangesToStorageChannelWhenRemoving() {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final Storage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));
        final StorageChannel<String> storageChannel = new StorageChannelImpl<>();
        storageChannel.addSource(sut);

        // Act
        storageChannel.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.removeSource(sut);
        final long insertedStraightIntoExternalStorage = sut.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final long insertedIntoStorageChannel = storageChannel.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(insertedStraightIntoExternalStorage).isEqualTo(10);
        assertThat(insertedIntoStorageChannel).isZero();
        assertThat(sut.getAll()).isNotEmpty();
        assertThat(sut.getStored()).isEqualTo(25);
        assertThat(storageChannel.getAll()).isEmpty();
        assertThat(storageChannel.getStored()).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsertAndDetectAndPropagateChanges(final Action action) {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final Storage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));
        final StorageChannel<String> storageChannel = new StorageChannelImpl<>();
        storageChannel.addSource(sut);

        // Act
        final long insertedA1 = storageChannel.insert("A", 10, action, EmptyActor.INSTANCE);
        final long insertedA2 = storageChannel.insert("A", 1, action, EmptyActor.INSTANCE);
        final long insertedB = storageChannel.insert("B", 5, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(insertedA1).isEqualTo(10);
        assertThat(insertedA2).isEqualTo(1);
        assertThat(insertedB).isEqualTo(5);

        if (action == Action.EXECUTE) {
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A!", 11),
                new ResourceAmount<>("B!", 5)
            );
            assertThat(storageChannel.getStored()).isEqualTo(16);
        } else {
            assertThat(storageChannel.getAll()).isEmpty();
            assertThat(storageChannel.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractPartiallyAndDetectAndPropagateChanges(final Action action) {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final Storage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));
        sut.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.insert("A2", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.insert("B", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final StorageChannel<String> storageChannel = new StorageChannelImpl<>();
        storageChannel.addSource(sut);

        // Act
        // this will try to extract A!(5) and A2!(5/2)
        final long extracted = storageChannel.extract("A!", 5, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(5);

        if (action == Action.EXECUTE) {
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A!", 5),
                new ResourceAmount<>("A2!", 8),
                new ResourceAmount<>("B!", 10)
            );
            assertThat(storageChannel.getStored()).isEqualTo(23);
        } else {
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A!", 10),
                new ResourceAmount<>("A2!", 10),
                new ResourceAmount<>("B!", 10)
            );
            assertThat(storageChannel.getStored()).isEqualTo(30);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractCompletelyAndDetectAndPropagateChanges(final Action action) {
        // Arrange
        final Storage<String> storage = new TransformingStorage();
        final Storage<String> sut = new ExternalStorage<>(new ExternalStorageProviderImpl<>(storage));
        sut.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.insert("A2", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.insert("B", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final StorageChannel<String> storageChannel = new StorageChannelImpl<>();
        storageChannel.addSource(sut);

        // Act
        // this will try to extract A!(10) and A2!(10/2)
        final long extracted = storageChannel.extract("A!", 10, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(10);

        if (action == Action.EXECUTE) {
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A2!", 5),
                new ResourceAmount<>("B!", 10)
            );
            assertThat(storageChannel.getStored()).isEqualTo(15);
        } else {
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A!", 10),
                new ResourceAmount<>("A2!", 10),
                new ResourceAmount<>("B!", 10)
            );
            assertThat(storageChannel.getStored()).isEqualTo(30);
        }
    }
}
