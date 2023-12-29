package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.verification.VerificationMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class StateTrackedStorageTest {
    @Test
    void shouldSetInitialState() {
        // Arrange
        final StateTrackedStorage.Listener listener = mock(StateTrackedStorage.Listener.class);
        final Storage<String> underlyingStorage = new LimitedStorageImpl<>(100);
        underlyingStorage.insert("A", 75, Action.EXECUTE, EmptyActor.INSTANCE);
        final StateTrackedStorage<String> sut = new StateTrackedStorage<>(underlyingStorage, listener);

        // Act
        final StorageState state = sut.getState();

        // Assert
        verify(listener, never()).onStorageStateChanged();
        assertThat(state).isEqualTo(StorageState.NEAR_CAPACITY);
    }

    @Test
    void shouldUseStorageTracking() {
        // Arrange
        final Storage<String> underlyingStorage = new TrackedStorageImpl<>(
            new LimitedStorageImpl<>(100),
            () -> 0L
        );
        final StateTrackedStorage<String> sut = new StateTrackedStorage<>(underlyingStorage, null);

        // Act
        sut.insert("A", 75, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(sut.findTrackedResourceByActorType("A", EmptyActor.class)).isNotEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldCallStateChangeListenerWhenExtracting(final Action action) {
        // Arrange
        final StateTrackedStorage.Listener listener = mock(StateTrackedStorage.Listener.class);
        final Storage<String> underlyingStorage = new LimitedStorageImpl<>(100);
        underlyingStorage.insert("A", 75, Action.EXECUTE, EmptyActor.INSTANCE);
        final StateTrackedStorage<String> sut = new StateTrackedStorage<>(underlyingStorage, listener);

        // Act
        sut.extract("A", 1, action, EmptyActor.INSTANCE);
        sut.extract("A", 1, action, EmptyActor.INSTANCE);

        // Assert
        final VerificationMode expectedTimes = action == Action.EXECUTE ? times(1) : never();
        verify(listener, expectedTimes).onStorageStateChanged();
        assertThat(sut.findTrackedResourceByActorType("A", EmptyActor.class)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldCallStateChangeListenerWhenInserting(final Action action) {
        // Arrange
        final StateTrackedStorage.Listener listener = mock(StateTrackedStorage.Listener.class);
        final Storage<String> underlyingStorage = new LimitedStorageImpl<>(100);
        underlyingStorage.insert("A", 74, Action.EXECUTE, EmptyActor.INSTANCE);
        final StateTrackedStorage<String> sut = new StateTrackedStorage<>(underlyingStorage, listener);

        // Act
        sut.insert("A", 1, action, EmptyActor.INSTANCE);
        sut.insert("A", 1, action, EmptyActor.INSTANCE);

        // Assert
        final VerificationMode expectedTimes = action == Action.EXECUTE ? times(1) : never();
        verify(listener, expectedTimes).onStorageStateChanged();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotCallStateChangeListenerWhenUnnecessaryOnExtracting(final Action action) {
        // Arrange
        final StateTrackedStorage.Listener listener = mock(StateTrackedStorage.Listener.class);
        final Storage<String> underlyingStorage = new LimitedStorageImpl<>(100);
        underlyingStorage.insert("A", 76, Action.EXECUTE, EmptyActor.INSTANCE);
        final StateTrackedStorage<String> sut = new StateTrackedStorage<>(underlyingStorage, listener);

        // Act
        sut.extract("A", 1, action, EmptyActor.INSTANCE);

        // Assert
        verify(listener, never()).onStorageStateChanged();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotCallStateChangeListenerWhenUnnecessaryOnInserting(final Action action) {
        // Arrange
        final StateTrackedStorage.Listener listener = mock(StateTrackedStorage.Listener.class);
        final Storage<String> underlyingStorage = new LimitedStorageImpl<>(100);
        final StateTrackedStorage<String> sut = new StateTrackedStorage<>(underlyingStorage, listener);

        // Act
        sut.insert("A", 74, action, EmptyActor.INSTANCE);

        // Assert
        verify(listener, never()).onStorageStateChanged();
    }
}
