package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.verification.VerificationMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class StateTrackedStorageTest {
    @ParameterizedTest
    @MethodSource("states")
    void testStates(final long amount, final StorageState expectedState) {
        // Arrange
        final StateTrackedStorage.Listener listener = mock(StateTrackedStorage.Listener.class);
        final Storage underlyingStorage = new LimitedStorageImpl(100);
        underlyingStorage.insert(TestResource.A, amount, Action.EXECUTE, EmptyActor.INSTANCE);
        final StateTrackedStorage sut = new StateTrackedStorage(underlyingStorage, listener);

        // Act
        final StorageState state = sut.getState();

        // Assert
        assertThat(state).isEqualTo(expectedState);
    }

    private static Stream<Arguments> states() {
        return Stream.of(
            Arguments.of(1L, StorageState.NORMAL),
            Arguments.of(74L, StorageState.NORMAL),
            Arguments.of(75L, StorageState.NEAR_CAPACITY),
            Arguments.of(99L, StorageState.NEAR_CAPACITY),
            Arguments.of(100L, StorageState.FULL)
        );
    }

    @Test
    void shouldSetInitialState() {
        // Arrange
        final StateTrackedStorage.Listener listener = mock(StateTrackedStorage.Listener.class);
        final Storage underlyingStorage = new InMemoryStorageImpl();
        underlyingStorage.insert(TestResource.A, 75, Action.EXECUTE, EmptyActor.INSTANCE);
        final StateTrackedStorage sut = new StateTrackedStorage(underlyingStorage, listener);

        // Act
        final StorageState state = sut.getState();

        // Assert
        verify(listener, never()).onStorageStateChanged();
        assertThat(state).isEqualTo(StorageState.NORMAL);
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(TestResource.A, 75)
        );
        assertThat(sut.getStored()).isEqualTo(75);
    }


    @Test
    void shouldSetInitialStateForLimitedStorage() {
        // Arrange
        final StateTrackedStorage.Listener listener = mock(StateTrackedStorage.Listener.class);
        final Storage underlyingStorage = new LimitedStorageImpl(100);
        underlyingStorage.insert(TestResource.A, 75, Action.EXECUTE, EmptyActor.INSTANCE);
        final StateTrackedStorage sut = new StateTrackedStorage(underlyingStorage, listener);

        // Act
        final StorageState state = sut.getState();

        // Assert
        verify(listener, never()).onStorageStateChanged();
        assertThat(state).isEqualTo(StorageState.NEAR_CAPACITY);
    }

    @Test
    void shouldUseStorageTracking() {
        // Arrange
        final Storage underlyingStorage = new TrackedStorageImpl(
            new LimitedStorageImpl(100),
            () -> 0L
        );
        final StateTrackedStorage sut = new StateTrackedStorage(underlyingStorage, null);

        // Act
        sut.insert(TestResource.A, 75, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(sut.findTrackedResourceByActorType(TestResource.A, EmptyActor.class)).isNotEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldCallStateChangeListenerWhenExtracting(final Action action) {
        // Arrange
        final StateTrackedStorage.Listener listener = mock(StateTrackedStorage.Listener.class);
        final Storage underlyingStorage = new LimitedStorageImpl(100);
        underlyingStorage.insert(TestResource.A, 75, Action.EXECUTE, EmptyActor.INSTANCE);
        final StateTrackedStorage sut = new StateTrackedStorage(underlyingStorage, listener);

        // Act
        final long extracted = sut.extract(TestResource.A, 1, action, EmptyActor.INSTANCE);
        sut.extract(TestResource.A, 1, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(1);
        final VerificationMode expectedTimes = action == Action.EXECUTE ? times(1) : never();
        verify(listener, expectedTimes).onStorageStateChanged();
        assertThat(sut.findTrackedResourceByActorType(TestResource.A, EmptyActor.class)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldCallStateChangeListenerWhenInserting(final Action action) {
        // Arrange
        final StateTrackedStorage.Listener listener = mock(StateTrackedStorage.Listener.class);
        final Storage underlyingStorage = new LimitedStorageImpl(100);
        underlyingStorage.insert(TestResource.A, 74, Action.EXECUTE, EmptyActor.INSTANCE);
        final StateTrackedStorage sut = new StateTrackedStorage(underlyingStorage, listener);

        // Act
        final long inserted = sut.insert(TestResource.A, 1, action, EmptyActor.INSTANCE);
        sut.insert(TestResource.A, 1, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(1);
        final VerificationMode expectedTimes = action == Action.EXECUTE ? times(1) : never();
        verify(listener, expectedTimes).onStorageStateChanged();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotCallStateChangeListenerWhenUnnecessaryOnExtracting(final Action action) {
        // Arrange
        final StateTrackedStorage.Listener listener = mock(StateTrackedStorage.Listener.class);
        final Storage underlyingStorage = new LimitedStorageImpl(100);
        underlyingStorage.insert(TestResource.A, 76, Action.EXECUTE, EmptyActor.INSTANCE);
        final StateTrackedStorage sut = new StateTrackedStorage(underlyingStorage, listener);

        // Act
        sut.extract(TestResource.A, 1, action, EmptyActor.INSTANCE);

        // Assert
        verify(listener, never()).onStorageStateChanged();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotCallStateChangeListenerWhenUnnecessaryOnInserting(final Action action) {
        // Arrange
        final StateTrackedStorage.Listener listener = mock(StateTrackedStorage.Listener.class);
        final Storage underlyingStorage = new LimitedStorageImpl(100);
        final StateTrackedStorage sut = new StateTrackedStorage(underlyingStorage, listener);

        // Act
        sut.insert(TestResource.A, 74, action, EmptyActor.INSTANCE);

        // Assert
        verify(listener, never()).onStorageStateChanged();
    }
}
