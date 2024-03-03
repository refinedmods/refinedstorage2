package com.refinedmods.refinedstorage2.api.grid.watcher;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Set;

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
    StorageChannelType storageChannelType = StorageChannelImpl::new;
    StorageChannel storageChannel;
    GridStorageChannelProvider storageChannelProvider;

    @BeforeEach
    void setUp() {
        sut = new GridWatcherManagerImpl();
        storageChannel = new StorageChannelImpl();
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannelProvider = new GridStorageChannelProvider() {
            @Override
            public Set<StorageChannelType> getStorageChannelTypes() {
                return Set.of(storageChannelType);
            }

            @Override
            public StorageChannel getStorageChannel(final StorageChannelType type) {
                if (type == storageChannelType) {
                    return storageChannel;
                }
                throw new IllegalArgumentException();
            }
        };
    }

    @Test
    void shouldAddWatcherAndNotifyOfChanges() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        storageChannel.insert(A, 10, Action.EXECUTE, FakeActor.INSTANCE);

        // Act
        sut.addWatcher(watcher, FakeActor.class, storageChannelProvider);
        storageChannel.insert(B, 5, Action.EXECUTE, FakeActor.INSTANCE);

        // Assert
        verify(watcher, times(1)).onChanged(storageChannelType, B, 5, null);
        verifyNoMoreInteractions(watcher);
    }

    @Test
    void shouldNotAddDuplicateWatcher() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        sut.addWatcher(watcher, FakeActor.class, storageChannelProvider);

        // Act & assert
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.addWatcher(watcher, FakeActor.class, storageChannelProvider),
            "Watcher is already registered"
        );
    }

    @Test
    void shouldRemoveWatcher() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        storageChannel.insert(A, 10, Action.EXECUTE, FakeActor.INSTANCE);
        sut.addWatcher(watcher, FakeActor.class, storageChannelProvider);

        // Act
        sut.removeWatcher(watcher, storageChannelProvider);
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
            () -> sut.removeWatcher(watcher, storageChannelProvider),
            "Watcher is not registered"
        );
    }

    @Test
    void shouldAddAndRemoveAndAddWatcherAgain() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        storageChannel.insert(A, 10, Action.EXECUTE, FakeActor.INSTANCE);

        // Act
        sut.addWatcher(watcher, FakeActor.class, storageChannelProvider);
        storageChannel.insert(B, 5, Action.EXECUTE, FakeActor.INSTANCE);
        sut.removeWatcher(watcher, storageChannelProvider);
        storageChannel.insert(C, 4, Action.EXECUTE, FakeActor.INSTANCE);
        sut.addWatcher(watcher, FakeActor.class, storageChannelProvider);
        storageChannel.insert(D, 3, Action.EXECUTE, FakeActor.INSTANCE);

        // Assert
        verify(watcher, times(1)).onChanged(storageChannelType, B, 5, null);
        verify(watcher, times(1)).onChanged(storageChannelType, D, 3, null);
        verifyNoMoreInteractions(watcher);
    }

    @Test
    void shouldDetachAll() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        storageChannel.insert(A, 10, Action.EXECUTE, FakeActor.INSTANCE);
        sut.addWatcher(watcher, FakeActor.class, storageChannelProvider);

        // Act
        sut.detachAll(storageChannelProvider);
        storageChannel.insert(B, 10, Action.EXECUTE, FakeActor.INSTANCE);
        assertThrows(IllegalArgumentException.class, () -> sut.addWatcher(
            watcher,
            FakeActor.class,
            storageChannelProvider
        ), "Watcher is already registered");

        // Assert
        verifyNoInteractions(watcher);
    }

    @Test
    void shouldAttachAll() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        storageChannel.insert(A, 10, Action.EXECUTE, FakeActor.INSTANCE);
        sut.addWatcher(watcher, FakeActor.class, storageChannelProvider);
        sut.detachAll(storageChannelProvider);
        storageChannel.insert(B, 5, Action.EXECUTE, FakeActor.INSTANCE);

        // Act
        sut.attachAll(storageChannelProvider);
        storageChannel.insert(C, 4, Action.EXECUTE, FakeActor.INSTANCE);

        // Assert
        final InOrder inOrder = inOrder(watcher);
        inOrder.verify(watcher, times(1)).invalidate();
        inOrder.verify(watcher, times(1)).onChanged(storageChannelType, A, 10, null);
        inOrder.verify(watcher, times(1)).onChanged(storageChannelType, B, 5, null);
        inOrder.verify(watcher, times(1)).onChanged(storageChannelType, C, 4, null);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void shouldNotifyAboutActivenessChange() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        sut.activeChanged(true);
        sut.addWatcher(watcher, FakeActor.class, storageChannelProvider);

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
