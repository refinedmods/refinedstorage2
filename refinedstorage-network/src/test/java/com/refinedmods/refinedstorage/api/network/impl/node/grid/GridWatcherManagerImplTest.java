package com.refinedmods.refinedstorage.api.network.impl.node.grid;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusListener;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusProvider;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.node.grid.GridWatcher;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.D;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class GridWatcherManagerImplTest {
    GridWatcherManager sut;
    RootStorage rootStorage;
    TaskStatusProviderImpl taskStatusProvider;

    @BeforeEach
    void setUp() {
        sut = new GridWatcherManagerImpl();
        rootStorage = new RootStorageImpl();
        rootStorage.addSource(new StorageImpl());
        taskStatusProvider = new TaskStatusProviderImpl();
    }

    @Test
    void shouldNotAddDuplicateWatcher() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        sut.addWatcher(watcher, ActorFixture.class, rootStorage, null);

        // Act & assert
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.addWatcher(watcher, ActorFixture.class, rootStorage, null),
            "Watcher is already registered"
        );
    }

    @Test
    void shouldNotRemoveWatcherThatIsNotRegistered() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);

        // Act & assert
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.removeWatcher(watcher, rootStorage, null),
            "Watcher is not registered"
        );
    }

    @Test
    void shouldNotifyAboutActivenessChange() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        sut.activeChanged(true);
        sut.addWatcher(watcher, ActorFixture.class, rootStorage, null);

        // Act
        sut.activeChanged(false);
        sut.activeChanged(true);

        // Assert
        final InOrder inOrder = inOrder(watcher);
        inOrder.verify(watcher, times(1)).onActiveChanged(false);
        inOrder.verify(watcher, times(1)).onActiveChanged(true);
        inOrder.verifyNoMoreInteractions();
    }


    private static TaskStatus taskStatus() {
        return new TaskStatus(
            new TaskStatus.TaskInfo(new TaskId(UUID.randomUUID()), A, 1, 0),
            TaskState.READY,
            0.0,
            Collections.emptyList()
        );
    }

    @Nested
    class StorageWatching {
        @Test
        void shouldAddWatcherAndNotifyOfChanges() {
            // Arrange
            final GridWatcher watcher = mock(GridWatcher.class);
            rootStorage.insert(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);

            // Act
            sut.addWatcher(watcher, ActorFixture.class, rootStorage, null);
            rootStorage.insert(B, 5, Action.EXECUTE, ActorFixture.INSTANCE);

            // Assert
            verify(watcher, times(1)).onChanged(B, 5, null);
            verifyNoMoreInteractions(watcher);
        }

        @Test
        void shouldRemoveWatcher() {
            // Arrange
            final GridWatcher watcher = mock(GridWatcher.class);
            rootStorage.insert(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);
            sut.addWatcher(watcher, ActorFixture.class, rootStorage, null);

            // Act
            sut.removeWatcher(watcher, rootStorage, null);
            rootStorage.insert(B, 5, Action.EXECUTE, ActorFixture.INSTANCE);

            // Assert
            verifyNoInteractions(watcher);
        }

        @Test
        void shouldAddAndRemoveAndAddWatcherAgain() {
            // Arrange
            final GridWatcher watcher = mock(GridWatcher.class);
            rootStorage.insert(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);

            // Act
            sut.addWatcher(watcher, ActorFixture.class, rootStorage, null);
            rootStorage.insert(B, 5, Action.EXECUTE, ActorFixture.INSTANCE);
            sut.removeWatcher(watcher, rootStorage, null);
            rootStorage.insert(C, 4, Action.EXECUTE, ActorFixture.INSTANCE);
            sut.addWatcher(watcher, ActorFixture.class, rootStorage, null);
            rootStorage.insert(D, 3, Action.EXECUTE, ActorFixture.INSTANCE);

            // Assert
            verify(watcher, times(1)).onChanged(B, 5, null);
            verify(watcher, times(1)).onChanged(D, 3, null);
            verifyNoMoreInteractions(watcher);
        }

        @Test
        void shouldDetachAll() {
            // Arrange
            final GridWatcher watcher = mock(GridWatcher.class);
            rootStorage.insert(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);
            sut.addWatcher(watcher, ActorFixture.class, rootStorage, null);

            // Act
            sut.detachAll(rootStorage, null);
            rootStorage.insert(B, 10, Action.EXECUTE, ActorFixture.INSTANCE);

            // Assert
            verifyNoInteractions(watcher);
        }

        @Test
        void shouldAttachAllAndReplay() {
            // Arrange
            final GridWatcher watcher = mock(GridWatcher.class);
            rootStorage.insert(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);
            sut.addWatcher(watcher, ActorFixture.class, rootStorage, null);
            sut.detachAll(rootStorage, null);
            rootStorage.insert(B, 5, Action.EXECUTE, ActorFixture.INSTANCE);

            // Act
            sut.attachAll(rootStorage, null);
            rootStorage.insert(C, 4, Action.EXECUTE, ActorFixture.INSTANCE);

            // Assert
            final InOrder inOrder = inOrder(watcher);
            inOrder.verify(watcher, times(1)).invalidate();
            verify(watcher, times(1)).onChanged(A, 10, null);
            verify(watcher, times(1)).onChanged(B, 5, null);
            verify(watcher, times(1)).onChanged(C, 4, null);
            verifyNoMoreInteractions(watcher);
        }

        @Test
        void shouldAttachAllWithoutStorage() {
            // Arrange
            final GridWatcher watcher = mock(GridWatcher.class);
            sut.addWatcher(watcher, ActorFixture.class, rootStorage, taskStatusProvider);
            sut.detachAll(rootStorage, taskStatusProvider);
            final TaskStatus existing = taskStatus();
            taskStatusProvider.addStatus(existing);

            // Act
            sut.attachAll(null, taskStatusProvider);
            rootStorage.insert(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);

            // Assert
            final InOrder inOrder = inOrder(watcher);
            inOrder.verify(watcher, times(1)).invalidate();
            verify(watcher, times(1)).taskAdded(existing);
            verifyNoMoreInteractions(watcher);
        }
    }

    @Nested
    class TaskStatusWatching {
        @Test
        void shouldAddWatcherAndNotifyOfChanges() {
            // Arrange
            final GridWatcher watcher = mock(GridWatcher.class);
            final TaskStatus existing = taskStatus();
            taskStatusProvider.addStatus(existing);

            // Act
            sut.addWatcher(watcher, ActorFixture.class, rootStorage, taskStatusProvider);
            // Existing task statuses should NOT be replayed on a normal add (no replay).
            final TaskStatus newStatus = taskStatus();
            taskStatusProvider.addStatus(newStatus);
            taskStatusProvider.changeStatus(newStatus);
            taskStatusProvider.removeStatus(newStatus.info().id());

            // Assert
            verify(watcher, times(1)).taskAdded(newStatus);
            verify(watcher, times(1)).taskStatusChanged(newStatus);
            verify(watcher, times(1)).taskRemoved(newStatus.info().id());
            verifyNoMoreInteractions(watcher);
        }

        @Test
        void shouldRemoveWatcher() {
            // Arrange
            final GridWatcher watcher = mock(GridWatcher.class);
            taskStatusProvider.addStatus(taskStatus());
            sut.addWatcher(watcher, ActorFixture.class, rootStorage, taskStatusProvider);

            // Act
            sut.removeWatcher(watcher, rootStorage, taskStatusProvider);
            taskStatusProvider.addStatus(taskStatus());

            // Assert
            verifyNoInteractions(watcher);
            assertThat(taskStatusProvider.listeners).isEmpty();
        }

        @Test
        void shouldAddAndRemoveAndAddWatcherAgain() {
            // Arrange
            final GridWatcher watcher = mock(GridWatcher.class);

            // Act
            sut.addWatcher(watcher, ActorFixture.class, rootStorage, taskStatusProvider);
            final TaskStatus first = taskStatus();
            taskStatusProvider.addStatus(first);
            sut.removeWatcher(watcher, rootStorage, taskStatusProvider);
            taskStatusProvider.addStatus(taskStatus());
            sut.addWatcher(watcher, ActorFixture.class, rootStorage, taskStatusProvider);
            final TaskStatus third = taskStatus();
            taskStatusProvider.addStatus(third);

            // Assert
            verify(watcher, times(1)).taskAdded(first);
            verify(watcher, times(1)).taskAdded(third);
            verifyNoMoreInteractions(watcher);
        }

        @Test
        void shouldDetachAll() {
            // Arrange
            final GridWatcher watcher = mock(GridWatcher.class);
            taskStatusProvider.addStatus(taskStatus());
            sut.addWatcher(watcher, ActorFixture.class, rootStorage, taskStatusProvider);

            // Act
            sut.detachAll(rootStorage, taskStatusProvider);
            taskStatusProvider.addStatus(taskStatus());

            // Assert
            verifyNoInteractions(watcher);
            assertThat(taskStatusProvider.listeners).isEmpty();
        }

        @Test
        void shouldAttachAllAndReplay() {
            // Arrange
            final GridWatcher watcher = mock(GridWatcher.class);
            final TaskStatus existing1 = taskStatus();
            final TaskStatus existing2 = taskStatus();
            taskStatusProvider.addStatus(existing1);
            sut.addWatcher(watcher, ActorFixture.class, rootStorage, taskStatusProvider);
            sut.detachAll(rootStorage, taskStatusProvider);
            taskStatusProvider.addStatus(existing2);

            // Act
            sut.attachAll(rootStorage, taskStatusProvider);
            final TaskStatus afterAttach = taskStatus();
            taskStatusProvider.addStatus(afterAttach);

            // Assert
            final InOrder inOrder = inOrder(watcher);
            inOrder.verify(watcher, times(1)).invalidate();
            verify(watcher, times(1)).taskAdded(existing1);
            verify(watcher, times(1)).taskAdded(existing2);
            verify(watcher, times(1)).taskAdded(afterAttach);
            verifyNoMoreInteractions(watcher);
        }

        @Test
        void shouldAttachAllWithoutTaskStatusProvider() {
            // Arrange
            final GridWatcher watcher = mock(GridWatcher.class);
            sut.addWatcher(watcher, ActorFixture.class, rootStorage, taskStatusProvider);
            sut.detachAll(rootStorage, taskStatusProvider);
            rootStorage.insert(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);

            // Act
            sut.attachAll(rootStorage, null);
            taskStatusProvider.addStatus(taskStatus());

            // Assert
            final InOrder inOrder = inOrder(watcher);
            inOrder.verify(watcher, times(1)).invalidate();
            verify(watcher, times(1)).onChanged(A, 10, null);
            verifyNoMoreInteractions(watcher);
        }
    }

    private static class ActorFixture implements Actor {
        public static final ActorFixture INSTANCE = new ActorFixture();

        private ActorFixture() {
        }

        @Override
        public String getName() {
            return "Fake";
        }
    }

    private static class TaskStatusProviderImpl implements TaskStatusProvider {
        private final List<TaskStatus> statuses = new ArrayList<>();
        private final Set<TaskStatusListener> listeners = new HashSet<>();

        @Override
        public List<TaskStatus> getStatuses() {
            return Collections.unmodifiableList(statuses);
        }

        @Override
        public void addListener(final TaskStatusListener listener) {
            listeners.add(listener);
        }

        @Override
        public void removeListener(final TaskStatusListener listener) {
            listeners.remove(listener);
        }

        @Override
        public void cancel(final TaskId taskId) {
            // no-op
        }

        @Override
        public void cancelAll() {
            // no-op
        }

        void addStatus(final TaskStatus status) {
            statuses.add(status);
            listeners.forEach(listener -> listener.taskAdded(status));
        }

        void changeStatus(final TaskStatus status) {
            listeners.forEach(listener -> listener.taskStatusChanged(status));
        }

        void removeStatus(final TaskId id) {
            statuses.removeIf(s -> s.info().id().equals(id));
            listeners.forEach(listener -> listener.taskRemoved(id));
        }
    }
}
