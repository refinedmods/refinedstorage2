package com.refinedmods.refinedstorage2.api.storage.external;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage2.api.storage.external.ExternalTestResource.A;
import static com.refinedmods.refinedstorage2.api.storage.external.ExternalTestResource.A_ALTERNATIVE;
import static com.refinedmods.refinedstorage2.api.storage.external.ExternalTestResource.A_TRANSFORMED;
import static com.refinedmods.refinedstorage2.api.storage.external.ExternalTestResource.B;
import static com.refinedmods.refinedstorage2.api.storage.external.ExternalTestResource.B_TRANSFORMED;
import static org.assertj.core.api.Assertions.assertThat;

class StorageChannelExternalStorageTest {
    SpyingExternalStorageListener listener;

    @BeforeEach
    void setUp() {
        listener = new SpyingExternalStorageListener();
    }

    @Test
    void shouldNotTakeExistingResourcesIntoConsiderationWhenBuildingInitialState() {
        // Arrange
        final Storage storage = new TransformingStorage();
        storage.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);
        final StorageChannel storageChannel = new StorageChannelImpl();

        // Act
        storageChannel.addSource(sut);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
        assertThat(storageChannel.getAll()).isEmpty();
        assertThat(storageChannel.getStored()).isZero();
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldTakeExistingResourcesIntoConsiderationWhenDetectingChanges() {
        // Arrange
        final Storage storage = new TransformingStorage();
        storage.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final ExternalStorage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);
        final StorageChannel storageChannel = new StorageChannelImpl();
        storageChannel.addSource(sut);

        // Act
        sut.detectChanges();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A_TRANSFORMED, 10)
        );
        assertThat(storageChannel.getStored()).isEqualTo(10);
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldNoLongerPropagateChangesToStorageChannelWhenRemoving() {
        // Arrange
        final Storage storage = new TransformingStorage();
        storage.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);
        final StorageChannel storageChannel = new StorageChannelImpl();
        storageChannel.addSource(sut);

        // Act
        storageChannel.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.removeSource(sut);
        final long insertedStraightIntoExternalStorage = sut.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final long insertedIntoStorageChannel = storageChannel.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(insertedStraightIntoExternalStorage).isEqualTo(10);
        assertThat(insertedIntoStorageChannel).isZero();
        assertThat(sut.getAll()).isNotEmpty();
        assertThat(sut.getStored()).isEqualTo(25);
        assertThat(storageChannel.getAll()).isEmpty();
        assertThat(storageChannel.getStored()).isZero();
        assertThat(listener.resources).containsExactly(A, A);
        assertThat(listener.actors).containsOnly(EmptyActor.INSTANCE);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsertAndDetectAndPropagateChanges(final Action action) {
        // Arrange
        final Storage storage = new TransformingStorage();
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);
        final StorageChannel storageChannel = new StorageChannelImpl();
        storageChannel.addSource(sut);

        // Act
        final long insertedA1 = storageChannel.insert(A, 10, action, EmptyActor.INSTANCE);
        final long insertedA2 = storageChannel.insert(A, 1, action, EmptyActor.INSTANCE);
        final long insertedB = storageChannel.insert(B, 5, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(insertedA1).isEqualTo(10);
        assertThat(insertedA2).isEqualTo(1);
        assertThat(insertedB).isEqualTo(5);

        if (action == Action.EXECUTE) {
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A_TRANSFORMED, 11),
                new ResourceAmount(B_TRANSFORMED, 5)
            );
            assertThat(storageChannel.getStored()).isEqualTo(16);
            assertThat(listener.resources).containsExactly(A, A, B);
            assertThat(listener.actors).containsOnly(EmptyActor.INSTANCE);
        } else {
            assertThat(storageChannel.getAll()).isEmpty();
            assertThat(storageChannel.getStored()).isZero();
            assertThat(listener.resources).isEmpty();
            assertThat(listener.actors).isEmpty();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractPartiallyAndDetectAndPropagateChanges(final Action action) {
        // Arrange
        final Storage storage = new TransformingStorage();
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);
        sut.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.insert(A_ALTERNATIVE, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.insert(B, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final StorageChannel storageChannel = new StorageChannelImpl();
        storageChannel.addSource(sut);

        // Act
        // this will try to extract A!(5) and A2!(5/2)
        final long extracted = storageChannel.extract(A_TRANSFORMED, 5, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(5);

        if (action == Action.EXECUTE) {
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A_TRANSFORMED, 5),
                new ResourceAmount(A_ALTERNATIVE, 8),
                new ResourceAmount(B_TRANSFORMED, 10)
            );
            assertThat(storageChannel.getStored()).isEqualTo(23);
            assertThat(listener.resources).containsExactly(A, A_ALTERNATIVE, B, A_TRANSFORMED);
            assertThat(listener.actors).containsOnly(EmptyActor.INSTANCE);
        } else {
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A_TRANSFORMED, 10),
                new ResourceAmount(A_ALTERNATIVE, 10),
                new ResourceAmount(B_TRANSFORMED, 10)
            );
            assertThat(storageChannel.getStored()).isEqualTo(30);
            assertThat(listener.resources).containsExactly(A, A_ALTERNATIVE, B);
            assertThat(listener.actors).containsOnly(EmptyActor.INSTANCE);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractCompletelyAndDetectAndPropagateChanges(final Action action) {
        // Arrange
        final Storage storage = new TransformingStorage();
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);
        sut.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.insert(A_ALTERNATIVE, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.insert(B, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final StorageChannel storageChannel = new StorageChannelImpl();
        storageChannel.addSource(sut);

        // Act
        // this will try to extract A!(10) and A2!(10/2)
        final long extracted = storageChannel.extract(A_TRANSFORMED, 10, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(10);

        if (action == Action.EXECUTE) {
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A_ALTERNATIVE, 5),
                new ResourceAmount(B_TRANSFORMED, 10)
            );
            assertThat(storageChannel.getStored()).isEqualTo(15);
            assertThat(listener.resources).containsExactly(A, A_ALTERNATIVE, B, A_TRANSFORMED);
            assertThat(listener.actors).containsOnly(EmptyActor.INSTANCE);
        } else {
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A_TRANSFORMED, 10),
                new ResourceAmount(A_ALTERNATIVE, 10),
                new ResourceAmount(B_TRANSFORMED, 10)
            );
            assertThat(storageChannel.getStored()).isEqualTo(30);
            assertThat(listener.resources).containsExactly(A, A_ALTERNATIVE, B);
            assertThat(listener.actors).containsOnly(EmptyActor.INSTANCE);
        }
    }
}
