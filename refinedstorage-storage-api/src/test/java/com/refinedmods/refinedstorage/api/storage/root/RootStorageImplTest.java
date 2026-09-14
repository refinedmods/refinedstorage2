package com.refinedmods.refinedstorage.api.storage.root;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.composite.PriorityStorage;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageImpl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import static com.refinedmods.refinedstorage.api.storage.TestResource.A;
import static com.refinedmods.refinedstorage.api.storage.TestResource.B;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RootStorageImplTest {
    private RootStorage sut;

    @BeforeEach
    void setUp() {
        sut = new RootStorageImpl(MutableResourceListImpl.create(), new LinkedHashSet<>());
    }

    @Test
    void shouldAddSource() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 8, Action.EXECUTE, Actor.EMPTY);

        // Act
        sut.addSource(storage);

        final long inserted = sut.insert(A, 3, Action.EXECUTE, Actor.EMPTY);

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
        storage.insert(A, 5, Action.EXECUTE, Actor.EMPTY);

        final Storage removedStorage = new LimitedStorageImpl(10);
        removedStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage);
        sut.addSource(removedStorage);

        // Act
        sut.removeSource(removedStorage);

        final long extracted = sut.extract(A, 15, Action.SIMULATE, Actor.EMPTY);

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
        matchedStorage.insert(A, 8, Action.EXECUTE, Actor.EMPTY);
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
        sut.insert(A, 2, Action.EXECUTE, Actor.EMPTY);

        final RootStorageListener listener = mock(RootStorageListener.class);
        sut.addListener(listener);

        final var changedResource = ArgumentCaptor.forClass(MutableResourceList.OperationResult.class);

        // Act
        sut.insert(A, 8, action, Actor.EMPTY);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).changed(changedResource.capture());

            assertThat(changedResource.getValue().change()).isEqualTo(8);
            assertThat(changedResource.getValue().resource()).isEqualTo(A);
            assertThat(changedResource.getValue().amount()).isEqualTo(10);
        } else {
            verify(listener, never()).changed(any());
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldCallListenerOnExtraction(final Action action) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage);
        sut.extract(A, 2, Action.EXECUTE, Actor.EMPTY);

        final RootStorageListener listener = mock(RootStorageListener.class);
        sut.addListener(listener);

        final var changedResource = ArgumentCaptor.forClass(MutableResourceList.OperationResult.class);

        // Act
        sut.extract(A, 5, action, Actor.EMPTY);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).changed(changedResource.capture());

            assertThat(changedResource.getValue().change()).isEqualTo(-5);
            assertThat(changedResource.getValue().resource()).isEqualTo(A);
            assertThat(changedResource.getValue().amount()).isEqualTo(3);
        } else {
            verify(listener, never()).changed(any());
        }
    }

    @Test
    void shouldRemoveListener() {
        // Arrange
        sut.addSource(new LimitedStorageImpl(10));
        sut.insert(A, 2, Action.EXECUTE, Actor.EMPTY);

        final RootStorageListener listener = mock(RootStorageListener.class);
        sut.addListener(listener);

        // Act
        sut.removeListener(listener);
        sut.insert(A, 8, Action.EXECUTE, Actor.EMPTY);

        // Assert
        verify(listener, never()).changed(any());
    }

    @Test
    void shouldInsert() {
        // Arrange
        sut.addSource(new LimitedStorageImpl(10));

        // Act
        final long inserted1 = sut.insert(A, 5, Action.EXECUTE, Actor.EMPTY);
        final long inserted2 = sut.insert(B, 4, Action.EXECUTE, Actor.EMPTY);

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
    void shouldInterceptInsertionPartiallyWithSingleListener() {
        // Arrange
        sut.addSource(new StorageImpl());
        final RootStorageListener listener = mock(RootStorageListener.class);
        when(listener.beforeInsert(A, 10)).thenReturn(3L);
        sut.addListener(listener);

        // Act
        final long inserted = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 7)
        );
        assertThat(inserted).isEqualTo(10);
    }

    @Test
    void shouldInterceptInsertionCompletelyWithSingleListener() {
        // Arrange
        sut.addSource(new StorageImpl());
        final RootStorageListener listener = mock(RootStorageListener.class);
        when(listener.beforeInsert(A, 10)).thenReturn(10L);
        sut.addListener(listener);

        // Act
        final long inserted = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(inserted).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(longs = {11, -1})
    void shouldDetectWhenListenerIsInterceptingMoreThanIsAvailableWithSingleListener(final long invalidReservation) {
        // Arrange
        sut.addSource(new StorageImpl());
        final RootStorageListener listener = mock(RootStorageListener.class);
        when(listener.beforeInsert(A, 10)).thenReturn(invalidReservation);
        sut.addListener(listener);

        // Act
        final ThrowableAssert.ThrowingCallable action = () -> sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThatThrownBy(action)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Intercepted %d while 10 was available", invalidReservation);
    }

    @Test
    void shouldInterceptInsertionPartiallyWithMultipleListeners() {
        // Arrange
        sut.addSource(new StorageImpl());

        final RootStorageListener listener1 = mock(RootStorageListener.class);
        when(listener1.beforeInsert(A, 10)).thenReturn(3L);
        sut.addListener(listener1);

        final RootStorageListener listener2 = mock(RootStorageListener.class);
        when(listener2.beforeInsert(A, 7)).thenReturn(5L);
        sut.addListener(listener2);

        // Act
        final long inserted = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 2)
        );
        assertThat(inserted).isEqualTo(10);
    }

    @Test
    void shouldInterceptInsertionCompletelyWithMultipleListeners() {
        // Arrange
        sut.addSource(new StorageImpl());

        final RootStorageListener listener1 = mock(RootStorageListener.class);
        when(listener1.beforeInsert(A, 10)).thenReturn(3L);
        sut.addListener(listener1);

        final RootStorageListener listener2 = mock(RootStorageListener.class);
        when(listener2.beforeInsert(A, 7)).thenReturn(7L);
        sut.addListener(listener2);

        final RootStorageListener listener3 = mock(RootStorageListener.class);
        sut.addListener(listener3);

        // Act
        final long inserted = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(inserted).isEqualTo(10);
        verify(listener3, never()).beforeInsert(any(), anyLong());
    }

    @ParameterizedTest
    @ValueSource(longs = {4, -1})
    void shouldDetectWhenListenerIsInterceptingMoreThanIsAvailableWithMultipleListeners(final long invalidReservation) {
        // Arrange
        sut.addSource(new StorageImpl());

        final RootStorageListener listener1 = mock(RootStorageListener.class);
        when(listener1.beforeInsert(A, 10)).thenReturn(7L);
        sut.addListener(listener1);

        final RootStorageListener listener2 = mock(RootStorageListener.class);
        when(listener2.beforeInsert(A, 3)).thenReturn(invalidReservation);
        sut.addListener(listener2);

        // Act
        final ThrowableAssert.ThrowingCallable action = () -> sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThatThrownBy(action)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Intercepted %d while 3 was available", invalidReservation);
    }

    @Test
    void shouldNotifySingleListenerOfInsertionAndReservePartially() {
        // Arrange
        sut.addSource(new StorageImpl());
        final RootStorageListener listener = mock(RootStorageListener.class);
        when(listener.beforeInsert(A, 10)).thenReturn(3L);
        when(listener.afterInsert(A, 7)).thenReturn(3L);
        sut.addListener(listener);

        // Act
        final long inserted = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 7)
        );
        assertThat(inserted).isEqualTo(10);
        verify(listener, times(1)).afterInsert(A, 7);
    }

    @Test
    void shouldNotifySingleListenerOfInsertionAndReserveCompletely() {
        // Arrange
        sut.addSource(new StorageImpl());
        final RootStorageListener listener = mock(RootStorageListener.class);
        when(listener.beforeInsert(A, 10)).thenReturn(3L);
        when(listener.afterInsert(A, 7)).thenReturn(7L);
        sut.addListener(listener);

        // Act
        final long inserted = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 7)
        );
        assertThat(inserted).isEqualTo(10);
        verify(listener, times(1)).afterInsert(A, 7);
    }

    @ParameterizedTest
    @ValueSource(longs = {8, -1})
    void shouldDetectWhenListenerIsReservingMoreThanIsAvailableWithSingleListener(final long invalidReservation) {
        // Arrange
        sut.addSource(new StorageImpl());
        final RootStorageListener listener = mock(RootStorageListener.class);
        when(listener.beforeInsert(A, 10)).thenReturn(3L);
        when(listener.afterInsert(A, 7)).thenReturn(invalidReservation);
        sut.addListener(listener);

        // Act
        final ThrowableAssert.ThrowingCallable action = () -> sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThatThrownBy(action)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Reserved %d while 7 was available", invalidReservation);
    }

    @Test
    void shouldNotifyMultipleListenersOfInsertionAndReservePartially() {
        // Arrange
        sut.addSource(new StorageImpl());

        final RootStorageListener listener1 = mock(RootStorageListener.class);
        when(listener1.beforeInsert(A, 10)).thenReturn(3L);
        when(listener1.afterInsert(A, 7)).thenReturn(3L);
        sut.addListener(listener1);

        final RootStorageListener listener2 = mock(RootStorageListener.class);
        when(listener2.afterInsert(A, 4)).thenReturn(1L);
        sut.addListener(listener2);

        // Act
        final long inserted = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 7)
        );
        assertThat(inserted).isEqualTo(10);
        verify(listener1, times(1)).afterInsert(A, 7);
        verify(listener2, times(1)).afterInsert(A, 4);
    }

    @Test
    void shouldNotifyMultipleListenersOfInsertionAndReserveCompletely() {
        // Arrange
        sut.addSource(new StorageImpl());

        final RootStorageListener listener1 = mock(RootStorageListener.class);
        when(listener1.beforeInsert(A, 10)).thenReturn(3L);
        when(listener1.afterInsert(A, 7)).thenReturn(3L);
        sut.addListener(listener1);

        final RootStorageListener listener2 = mock(RootStorageListener.class);
        when(listener2.afterInsert(A, 4)).thenReturn(4L);
        sut.addListener(listener2);

        final RootStorageListener listener3 = mock(RootStorageListener.class);
        sut.addListener(listener3);

        // Act
        final long inserted = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 7)
        );
        assertThat(inserted).isEqualTo(10);
        verify(listener1, times(1)).afterInsert(A, 7);
        verify(listener2, times(1)).afterInsert(A, 4);
        verify(listener3, never()).afterInsert(any(), anyLong());
    }

    @ParameterizedTest
    @ValueSource(longs = {5, -1})
    void shouldDetectWhenListenerIsReservingMoreThanIsAvailableWithMultipleListeners(final long invalidReservation) {
        // Arrange
        sut.addSource(new StorageImpl());

        final RootStorageListener listener1 = mock(RootStorageListener.class);
        when(listener1.beforeInsert(A, 10)).thenReturn(3L);
        when(listener1.afterInsert(A, 7)).thenReturn(3L);
        sut.addListener(listener1);

        final RootStorageListener listener2 = mock(RootStorageListener.class);
        when(listener2.afterInsert(A, 4)).thenReturn(invalidReservation);
        sut.addListener(listener2);

        // Act
        final ThrowableAssert.ThrowingCallable action = () -> sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThatThrownBy(action)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Reserved %d while 4 was available", invalidReservation);
    }

    @Test
    void shouldNotNotifyListenerWhenNothingWasInserted() {
        // Arrange
        sut.addSource(new StorageImpl() {
            @Override
            public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
                if (amount == 7) {
                    return 0;
                }
                throw new IllegalArgumentException();
            }
        });
        final RootStorageListener listener = mock(RootStorageListener.class);
        when(listener.beforeInsert(A, 10)).thenReturn(3L);
        sut.addListener(listener);

        // Act
        final long inserted = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(inserted).isEqualTo(3);
        verify(listener, never()).afterInsert(any(), anyLong());
    }

    @Test
    void shouldNotNotifyListenerWhenSimulating() {
        // Arrange
        sut.addSource(new StorageImpl());
        final RootStorageListener listener = mock(RootStorageListener.class);
        sut.addListener(listener);

        // Act
        final long inserted = sut.insert(A, 10, Action.SIMULATE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(inserted).isEqualTo(10);
        verify(listener, never()).beforeInsert(any(), anyLong());
        verify(listener, never()).afterInsert(any(), anyLong());
    }

    @Test
    void shouldExtract() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 50, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage);

        // Act
        final long extracted = sut.extract(A, 49, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );
        assertThat(extracted).isEqualTo(49);
        assertThat(sut.getStored()).isEqualTo(1);
    }

    @Test
    void shouldRetrieveIfResourceIsContained() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 50, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage);

        // Act & assert
        assertThat(sut.contains(A)).isTrue();
        assertThat(sut.contains(B)).isFalse();
    }

    @Test
    void shouldRetrieveResourceAmount() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 50, Action.EXECUTE, Actor.EMPTY);
        storage.extract(A, 25, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage);

        // Act & assert
        assertThat(sut.get(A)).isEqualTo(25);
        assertThat(sut.get(B)).isZero();
    }

    @Test
    void shouldRetrieveTrackedResource() {
        // Arrange
        final Storage storage = new TrackedStorageImpl(
            new LimitedStorageImpl(100),
            () -> 0L
        );

        sut.addSource(storage);

        // Act
        sut.insert(A, 50, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.contains(A)).isTrue();
        assertThat(sut.findTrackedResourceByActorType(A, Actor.EMPTY.getClass()))
            .get()
            .usingRecursiveComparison()
            .isEqualTo(new TrackedResource("Empty", 0));
        assertThat(sut.getTrackedResourcesByActorType(Actor.EMPTY.getClass(), Set.of(A))).containsOnlyKeys(A);
    }

    @Test
    void shouldSortSources() {
        // Arrange
        final PriorityStorage storage1 = PriorityStorage.of(new LimitedStorageImpl(10), 0, 0);
        final PriorityStorage storage2 = PriorityStorage.of(new LimitedStorageImpl(10), 0, 0);
        final PriorityStorage storage3 = PriorityStorage.of(new LimitedStorageImpl(10), 0, 0);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        storage1.setInsertPriority(8);
        storage2.setInsertPriority(15);
        storage3.setInsertPriority(2);

        storage1.setExtractPriority(8);
        storage2.setExtractPriority(2);
        storage3.setExtractPriority(15);

        // Act & assert
        sut.sortSources();

        sut.insert(A, 15, Action.EXECUTE, Actor.EMPTY);
        assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 5)
        );
        assertThat(storage3.getAll()).isEmpty();

        sut.extract(A, 12, Action.EXECUTE, Actor.EMPTY);
        assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 3)
        );
        assertThat(storage1.getAll()).isEmpty();
        assertThat(storage3.getAll()).isEmpty();
    }
}
