package com.refinedmods.refinedstorage2.api.grid.watcher;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static com.refinedmods.refinedstorage2.api.grid.TestResource.A;
import static com.refinedmods.refinedstorage2.api.grid.TestResource.B;
import static com.refinedmods.refinedstorage2.api.grid.TestResource.C;
import static com.refinedmods.refinedstorage2.api.grid.TestResource.D;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class GridWatcherManagerImplTest {
    GridWatcherManager sut;
    StorageChannel storageChannel;

    @BeforeEach
    void setUp() {
        sut = new GridWatcherManagerImpl();
        storageChannel = new StorageChannelImpl();
        storageChannel.addSource(new InMemoryStorageImpl());
    }

    @Test
    void shouldAddWatcherAndNotifyOfChanges() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        storageChannel.insert(A, 10, Action.EXECUTE, FakeActor.INSTANCE);

        // Act
        sut.addWatcher(watcher, FakeActor.class, storageChannel);
        storageChannel.insert(B, 5, Action.EXECUTE, FakeActor.INSTANCE);

        // Assert
        verify(watcher, times(1)).onChanged(B, 5, null);
        verifyNoMoreInteractions(watcher);
    }

    @Test
    void shouldNotAddDuplicateWatcher() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        sut.addWatcher(watcher, FakeActor.class, storageChannel);

        // Act & assert
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.addWatcher(watcher, FakeActor.class, storageChannel),
            "Watcher is already registered"
        );
    }

    @Test
    void shouldRemoveWatcher() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        storageChannel.insert(A, 10, Action.EXECUTE, FakeActor.INSTANCE);
        sut.addWatcher(watcher, FakeActor.class, storageChannel);

        // Act
        sut.removeWatcher(watcher, storageChannel);
        storageChannel.insert(B, 5, Action.EXECUTE, FakeActor.INSTANCE);

        // Assert
        verifyNoInteractions(watcher);
    }

    @Test
    void shouldNotRemoveWatcherThatIsNotRegistered() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);

        // Act & assert
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.removeWatcher(watcher, storageChannel),
            "Watcher is not registered"
        );
    }

    @Test
    void shouldAddAndRemoveAndAddWatcherAgain() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        storageChannel.insert(A, 10, Action.EXECUTE, FakeActor.INSTANCE);

        // Act
        sut.addWatcher(watcher, FakeActor.class, storageChannel);
        storageChannel.insert(B, 5, Action.EXECUTE, FakeActor.INSTANCE);
        sut.removeWatcher(watcher, storageChannel);
        storageChannel.insert(C, 4, Action.EXECUTE, FakeActor.INSTANCE);
        sut.addWatcher(watcher, FakeActor.class, storageChannel);
        storageChannel.insert(D, 3, Action.EXECUTE, FakeActor.INSTANCE);

        // Assert
        verify(watcher, times(1)).onChanged(B, 5, null);
        verify(watcher, times(1)).onChanged(D, 3, null);
        verifyNoMoreInteractions(watcher);
    }

    @Test
    void shouldDetachAll() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        storageChannel.insert(A, 10, Action.EXECUTE, FakeActor.INSTANCE);
        sut.addWatcher(watcher, FakeActor.class, storageChannel);

        // Act
        sut.detachAll(storageChannel);
        storageChannel.insert(B, 10, Action.EXECUTE, FakeActor.INSTANCE);
        assertThrows(IllegalArgumentException.class, () -> sut.addWatcher(
            watcher,
            FakeActor.class,
            storageChannel
        ), "Watcher is already registered");

        // Assert
        verifyNoInteractions(watcher);
    }

    @Test
    void shouldAttachAll() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        storageChannel.insert(A, 10, Action.EXECUTE, FakeActor.INSTANCE);
        sut.addWatcher(watcher, FakeActor.class, storageChannel);
        sut.detachAll(storageChannel);
        storageChannel.insert(B, 5, Action.EXECUTE, FakeActor.INSTANCE);

        // Act
        sut.attachAll(storageChannel);
        storageChannel.insert(C, 4, Action.EXECUTE, FakeActor.INSTANCE);

        // Assert
        final InOrder inOrder = inOrder(watcher);
        inOrder.verify(watcher, times(1)).invalidate();
        verify(watcher, times(1)).onChanged(A, 10, null);
        verify(watcher, times(1)).onChanged(B, 5, null);
        verify(watcher, times(1)).onChanged(C, 4, null);
        verifyNoMoreInteractions(watcher);
    }

    @Test
    void shouldNotifyAboutActivenessChange() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        sut.activeChanged(true);
        sut.addWatcher(watcher, FakeActor.class, storageChannel);

        // Act
        sut.activeChanged(false);
        sut.activeChanged(true);

        // Assert
        final InOrder inOrder = inOrder(watcher);
        inOrder.verify(watcher, times(1)).onActiveChanged(false);
        inOrder.verify(watcher, times(1)).onActiveChanged(true);
        inOrder.verifyNoMoreInteractions();
    }

    private static class FakeActor implements Actor {
        public static final FakeActor INSTANCE = new FakeActor();

        private FakeActor() {
        }

        @Override
        public String getName() {
            return "Fake";
        }
    }
}
