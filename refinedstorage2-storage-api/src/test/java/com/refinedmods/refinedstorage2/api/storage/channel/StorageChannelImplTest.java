package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.PrioritizedStorage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import static com.refinedmods.refinedstorage2.api.storage.TestResource.A;
import static com.refinedmods.refinedstorage2.api.storage.TestResource.B;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class StorageChannelImplTest {
    private StorageChannel sut;

    @BeforeEach
    void setUp() {
        sut = new StorageChannelImpl();
    }

    @Test
    void shouldAddSource() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 8, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        sut.addSource(storage);

        final long inserted = sut.insert(A, 3, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(inserted).isEqualTo(2);
    }

    @Test
    void shouldRemoveSource() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage removedStorage = new LimitedStorageImpl(10);
        removedStorage.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(storage);
        sut.addSource(removedStorage);

        // Act
        sut.removeSource(removedStorage);

        final long extracted = sut.extract(A, 15, Action.SIMULATE, EmptyActor.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5)
        );
        assertThat(extracted).isEqualTo(5);
    }

    @Test
    void shouldFindMatchingStorage() {
        // Arrange
        final Storage matchedStorage = new LimitedStorageImpl(10);
        matchedStorage.insert(A, 8, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.addSource(matchedStorage);

        final Storage unmatchedStorage = new LimitedStorageImpl(10);

        // Act
        final boolean foundMatched = sut.hasSource(s -> s == matchedStorage);
        final boolean foundUnmatched = sut.hasSource(s -> s == unmatchedStorage);

        // Assert
        assertThat(foundMatched).isTrue();
        assertThat(foundUnmatched).isFalse();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldCallListenerOnInsertion(final Action action) {
        // Arrange
        sut.addSource(new LimitedStorageImpl(10));
        sut.insert(A, 2, Action.EXECUTE, EmptyActor.INSTANCE);

        final ResourceListListener listener = mock(ResourceListListener.class);
        sut.addListener(listener);

        final var changedResource = ArgumentCaptor.forClass(ResourceListOperationResult.class);

        // Act
        sut.insert(A, 8, action, EmptyActor.INSTANCE);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).onChanged(changedResource.capture());

            assertThat(changedResource.getValue().change()).isEqualTo(8);
            assertThat(changedResource.getValue().resourceAmount()).usingRecursiveComparison().isEqualTo(
                new ResourceAmount(A, 10)
            );
        } else {
            verify(listener, never()).onChanged(any());
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldCallListenerOnExtraction(final Action action) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(storage);
        sut.extract(A, 2, Action.EXECUTE, EmptyActor.INSTANCE);

        final ResourceListListener listener = mock(ResourceListListener.class);
        sut.addListener(listener);

        final var changedResource = ArgumentCaptor.forClass(ResourceListOperationResult.class);

        // Act
        sut.extract(A, 5, action, EmptyActor.INSTANCE);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).onChanged(changedResource.capture());

            assertThat(changedResource.getValue().change()).isEqualTo(-5);
            assertThat(changedResource.getValue().resourceAmount()).usingRecursiveComparison().isEqualTo(
                new ResourceAmount(A, 3)
            );
        } else {
            verify(listener, never()).onChanged(any());
        }
    }

    @Test
    void shouldRemoveListener() {
        // Arrange
        sut.addSource(new LimitedStorageImpl(10));
        sut.insert(A, 2, Action.EXECUTE, EmptyActor.INSTANCE);

        final ResourceListListener listener = mock(ResourceListListener.class);
        sut.addListener(listener);

        // Act
        sut.removeListener(listener);
        sut.insert(A, 8, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        verify(listener, never()).onChanged(any());
    }

    @Test
    void shouldInsert() {
        // Arrange
        sut.addSource(new LimitedStorageImpl(10));

        // Act
        final long inserted1 = sut.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted2 = sut.insert(B, 4, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 4)
        );
        assertThat(inserted1).isEqualTo(5);
        assertThat(inserted2).isEqualTo(4);
        assertThat(sut.getStored()).isEqualTo(9);
    }

    @Test
    void shouldExtract() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 50, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(storage);

        // Act
        final long extracted = sut.extract(A, 49, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );
        assertThat(extracted).isEqualTo(49);
        assertThat(sut.getStored()).isEqualTo(1);
    }

    @Test
    void shouldBeAbleToRetrieveResource() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 50, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(storage);

        // Act
        final Optional<ResourceAmount> resource = sut.get(A);

        // Assert
        assertThat(resource).isPresent();
        assertThat(resource.get()).usingRecursiveComparison().isEqualTo(new ResourceAmount(A, 50));
        assertThat(sut.findTrackedResourceByActorType(A, EmptyActor.class)).isEmpty();
    }

    @Test
    void shouldBeAbleToRetrieveTrackedResource() {
        // Arrange
        final Storage storage = new TrackedStorageImpl(
            new LimitedStorageImpl(100),
            () -> 0L
        );

        sut.addSource(storage);

        // Act
        sut.insert(A, 50, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        final Optional<ResourceAmount> value = sut.get(A);
        assertThat(value).isPresent();
        assertThat(value.get()).usingRecursiveComparison().isEqualTo(new ResourceAmount(A, 50));

        assertThat(sut.findTrackedResourceByActorType(A, EmptyActor.class))
            .get()
            .usingRecursiveComparison()
            .isEqualTo(new TrackedResource("Empty", 0));
    }

    @Test
    void shouldNotBeAbleToRetrieveNonExistentResource() {
        // Arrange
        sut.addSource(new LimitedStorageImpl(100));

        // Act
        final Optional<ResourceAmount> resource = sut.get(A);

        // Assert
        assertThat(resource).isEmpty();
    }

    @Test
    void shouldSortSources() {
        // Arrange
        final PrioritizedStorage storage1 = new PrioritizedStorage(0, new LimitedStorageImpl(10));
        final PrioritizedStorage storage2 = new PrioritizedStorage(0, new LimitedStorageImpl(10));
        final PrioritizedStorage storage3 = new PrioritizedStorage(0, new LimitedStorageImpl(10));

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        storage1.setPriority(8);
        storage2.setPriority(15);
        storage3.setPriority(2);

        // Act
        sut.sortSources();

        sut.insert(A, 15, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 5)
        );
        assertThat(storage3.getAll()).isEmpty();
    }
}
