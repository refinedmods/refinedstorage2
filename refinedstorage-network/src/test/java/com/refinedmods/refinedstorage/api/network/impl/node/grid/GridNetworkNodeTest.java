package com.refinedmods.refinedstorage.api.network.impl.node.grid;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridWatcher;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetwork;
import com.refinedmods.refinedstorage.network.test.InjectNetworkAutocraftingComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;
import com.refinedmods.refinedstorage.network.test.fixtures.ActorFixture;
import com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.PatternProviderNetworkNodeFactory;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.D;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@NetworkTest
@SetupNetwork
@SetupNetwork(id = "other")
class GridNetworkNodeTest {
    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = AbstractNetworkNodeFactory.PROPERTY_ENERGY_USAGE, longValue = 5)
    })
    GridNetworkNode sut;

    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PatternProviderNetworkNodeFactory.PROPERTY_SIZE, intValue = 2)
    })
    PatternProviderNetworkNode patternProvider;

    @AddNetworkNode(networkId = "other", properties = {
        @AddNetworkNode.Property(key = PatternProviderNetworkNodeFactory.PROPERTY_SIZE, intValue = 2)
    })
    PatternProviderNetworkNode otherPatternProvider;

    @BeforeEach
    void setUp(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkStorageComponent(networkId = "other") final StorageNetworkComponent otherStorage
    ) {
        storage.addSource(new TrackedStorageImpl(new LimitedStorageImpl(1000), () -> 2L));
        storage.insert(A, 100, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 200, Action.EXECUTE, Actor.EMPTY);

        otherStorage.addSource(new TrackedStorageImpl(new StorageImpl(), () -> 3L));
    }

    @Test
    void testInitialState() {
        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(5);
    }

    @Test
    void shouldNotifyWatchersOfActivenessChanges() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);

        // Act
        sut.addWatcher(watcher, Actor.EMPTY.getClass());
        sut.setActive(true);
        sut.setActive(false);
        sut.removeWatcher(watcher);
        sut.setActive(true);
        sut.setActive(false);

        // Assert
        verify(watcher).onActiveChanged(true);
        verify(watcher).onActiveChanged(false);
        verifyNoMoreInteractions(watcher);
    }

    @Test
    void shouldNotBeAbleToRemoveUnknownWatcher() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.removeWatcher(watcher));
    }

    @Test
    void shouldNotBeAbleToAddDuplicateWatcher() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        final Class<? extends Actor> actorType1 = Actor.EMPTY.getClass();
        final Class<? extends Actor> actorType2 = ActorFixture.class;

        sut.addWatcher(watcher, actorType1);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.addWatcher(watcher, actorType1));
        assertThrows(IllegalArgumentException.class, () -> sut.addWatcher(watcher, actorType2));
    }

    @Test
    void shouldBeAbleToAddAndRemoveWatcherWithoutNetwork() {
        // Arrange
        sut.setNetwork(null);

        final GridWatcher watcher = mock(GridWatcher.class);

        // Act
        sut.addWatcher(watcher, Actor.EMPTY.getClass());
        sut.onActiveChanged(true);
        sut.removeWatcher(watcher);
        sut.onActiveChanged(false);

        // Assert
        verify(watcher).onActiveChanged(true);
        verifyNoMoreInteractions(watcher);
    }

    @Nested
    class StorageWatching {
        @Test
        void shouldNotifyWatchersOfChanges(
            @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
        ) {
            // Arrange
            final GridWatcher watcher = mock(GridWatcher.class);
            sut.addWatcher(watcher, ActorFixture.class);

            // Act
            networkStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
            networkStorage.insert(A, 1, Action.EXECUTE, ActorFixture.INSTANCE);
            sut.removeWatcher(watcher);
            networkStorage.insert(A, 1, Action.EXECUTE, ActorFixture.INSTANCE);

            // Assert
            final ArgumentCaptor<ResourceKey> resources = ArgumentCaptor.forClass(ResourceKey.class);
            final ArgumentCaptor<TrackedResource> trackedResources = ArgumentCaptor.forClass(TrackedResource.class);
            verify(watcher, times(2)).onChanged(
                resources.capture(),
                anyLong(),
                trackedResources.capture()
            );

            assertThat(resources.getAllValues()).containsExactly(A, A);
            assertThat(trackedResources.getAllValues())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(null, new TrackedResource("Fake", 2));

            verifyNoMoreInteractions(watcher);
        }

        @Test
        void shouldReplayOnceNetworkBecomesAvailableAfterAddingWatcherWithoutNetwork(
            @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
        ) {
            // Arrange
            final Network originalNetwork = sut.getNetwork();
            final GridWatcher watcher = mock(GridWatcher.class);

            // Act
            sut.setNetwork(null);
            sut.addWatcher(watcher, Actor.EMPTY.getClass());
            sut.onActiveChanged(true);

            sut.setNetwork(originalNetwork);
            sut.removeWatcher(watcher);

            sut.onActiveChanged(false);
            networkStorage.insert(C, 10, Action.EXECUTE, Actor.EMPTY);

            // Assert
            verify(watcher).onActiveChanged(true);
            verify(watcher).invalidate();
            verify(watcher).onChanged(eq(A), eq(100L), any());
            verify(watcher).onChanged(eq(B), eq(200L), any());
            verifyNoMoreInteractions(watcher);
        }

        @Test
        void shouldDetachWatcherFromOldNetworkAndReattachToNewNetwork(
            @InjectNetwork("other") final Network otherNetwork,
            @InjectNetwork final Network network,
            @InjectNetworkStorageComponent final StorageNetworkComponent storage,
            @InjectNetworkStorageComponent(networkId = "other") final StorageNetworkComponent otherStorage
        ) {
            // Arrange
            final GridWatcher watcher = mock(GridWatcher.class);
            sut.addWatcher(watcher, ActorFixture.class);

            // Act
            // This one shouldn't be ignored!
            otherStorage.insert(C, 10, Action.EXECUTE, ActorFixture.INSTANCE);

            sut.setNetwork(otherNetwork);
            network.removeContainer(() -> sut);
            otherNetwork.addContainer(() -> sut);

            // these one shouldn't be ignored either
            otherStorage.insert(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);
            otherStorage.insert(D, 10, Action.EXECUTE, Actor.EMPTY);

            // these should be ignored
            storage.insert(B, 10, Action.EXECUTE, ActorFixture.INSTANCE);
            storage.insert(D, 10, Action.EXECUTE, Actor.EMPTY);

            // Assert
            verify(watcher, times(1)).invalidate();

            final ArgumentCaptor<TrackedResource> trackedResources1 = ArgumentCaptor.forClass(TrackedResource.class);
            verify(watcher, times(1)).onChanged(
                eq(C),
                eq(10L),
                trackedResources1.capture()
            );
            assertThat(trackedResources1.getAllValues())
                .hasSize(1)
                .allMatch(t -> ActorFixture.INSTANCE.getName().equals(t.getSourceName()));

            final ArgumentCaptor<TrackedResource> trackedResources2 = ArgumentCaptor.forClass(TrackedResource.class);
            verify(watcher, times(1)).onChanged(
                eq(A),
                eq(10L),
                trackedResources2.capture()
            );
            assertThat(trackedResources2.getAllValues())
                .hasSize(1)
                .allMatch(t -> ActorFixture.INSTANCE.getName().equals(t.getSourceName()));

            verify(watcher, times(1)).onChanged(
                eq(D),
                eq(10L),
                isNull()
            );

            verifyNoMoreInteractions(watcher);
        }
    }

    @Nested
    class TaskStatusWatching {
        @Test
        void shouldNotifyWatchersOfChanges(
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            patternProvider.tryUpdatePattern(0, pattern().ingredient(A, 1).output(C, 1).build());
            patternProvider.tryUpdatePattern(1, pattern().ingredient(A, 1).output(D, 1).build());

            final GridWatcher watcher = mock(GridWatcher.class);
            sut.addWatcher(watcher, ActorFixture.class);

            // Act
            final Optional<TaskId> firstTask = autocrafting.startTask(C, 1, Actor.EMPTY, false,
                CancellationToken.NONE);
            sut.removeWatcher(watcher);
            final Optional<TaskId> secondTask = autocrafting.startTask(D, 1, Actor.EMPTY, false,
                CancellationToken.NONE);

            // Assert
            assertThat(firstTask).isPresent();
            assertThat(secondTask).isPresent();
            final ArgumentCaptor<TaskStatus> captor = ArgumentCaptor.forClass(TaskStatus.class);
            verify(watcher, times(1)).taskAdded(captor.capture());
            assertThat(captor.getValue().info().id()).isEqualTo(firstTask.get());
            verifyNoMoreInteractions(watcher);
        }

        @Test
        void shouldReplayOnceNetworkBecomesAvailableAfterAddingWatcherWithoutNetwork(
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            patternProvider.tryUpdatePattern(0, pattern().ingredient(A, 1).output(C, 1).build());
            patternProvider.tryUpdatePattern(1, pattern().ingredient(A, 1).output(D, 1).build());
            final Optional<TaskId> taskId = autocrafting.startTask(C, 1, Actor.EMPTY, false, CancellationToken.NONE);
            assertThat(taskId).isPresent();
            final Network originalNetwork = sut.getNetwork();
            final GridWatcher watcher = mock(GridWatcher.class);

            sut.setNetwork(null);
            sut.addWatcher(watcher, Actor.EMPTY.getClass());

            // Act
            sut.onActiveChanged(true);
            sut.setNetwork(originalNetwork);

            sut.removeWatcher(watcher);
            sut.onActiveChanged(false);
            autocrafting.startTask(D, 1, Actor.EMPTY, false, CancellationToken.NONE);

            // Assert
            verify(watcher).onActiveChanged(true);
            verify(watcher).invalidate();
            verify(watcher).onChanged(eq(A), eq(100L), any());
            verify(watcher).onChanged(eq(B), eq(200L), any());
            final ArgumentCaptor<TaskStatus> captor = ArgumentCaptor.forClass(TaskStatus.class);
            verify(watcher).taskAdded(captor.capture());
            assertThat(captor.getValue().info().id()).isEqualTo(taskId.get());
            verifyNoMoreInteractions(watcher);
        }

        @Test
        void shouldDetachWatcherFromOldNetworkAndReattachToNewNetwork(
            @InjectNetwork("other") final Network otherNetwork,
            @InjectNetwork final Network network,
            @InjectNetworkStorageComponent(networkId = "other") final StorageNetworkComponent otherStorage,
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting,
            @InjectNetworkAutocraftingComponent(networkId = "other")
            final AutocraftingNetworkComponent otherAutocrafting
        ) {
            // Arrange
            patternProvider.tryUpdatePattern(0, pattern().ingredient(A, 1).output(C, 1).build());
            otherPatternProvider.tryUpdatePattern(0, pattern().ingredient(A, 1).output(C, 1).build());
            otherPatternProvider.tryUpdatePattern(1, pattern().ingredient(A, 1).output(D, 1).build());

            final GridWatcher watcher = mock(GridWatcher.class);
            sut.addWatcher(watcher, ActorFixture.class);

            // Act
            // This task on the new network shouldn't be ignored - should be replayed during the swap!
            otherStorage.insert(A, 20, Action.EXECUTE, ActorFixture.INSTANCE);
            final Optional<TaskId> existingTaskOnOther = otherAutocrafting.startTask(
                C, 1, Actor.EMPTY, false, CancellationToken.NONE
            );
            assertThat(existingTaskOnOther).isPresent();

            sut.setNetwork(otherNetwork);
            network.removeContainer(() -> sut);
            otherNetwork.addContainer(() -> sut);

            // This task on the NEW network shouldn't be ignored either.
            final Optional<TaskId> newTaskOnOther = otherAutocrafting.startTask(
                D, 1, Actor.EMPTY, false, CancellationToken.NONE
            );
            assertThat(newTaskOnOther).isPresent();

            // This task on the OLD network should be ignored.
            autocrafting.startTask(C, 1, Actor.EMPTY, false, CancellationToken.NONE);

            // Assert
            verify(watcher, times(1)).invalidate();

            // Storage replay from the new network (A=20 with ActorFixture tracked resource).
            final ArgumentCaptor<TrackedResource> trackedResources = ArgumentCaptor.forClass(TrackedResource.class);
            verify(watcher, times(1)).onChanged(eq(A), eq(20L), trackedResources.capture());
            assertThat(trackedResources.getAllValues())
                .hasSize(1)
                .allMatch(t -> ActorFixture.INSTANCE.getName().equals(t.getSourceName()));

            // Task status replay (existing) + new task on new network.
            final ArgumentCaptor<TaskStatus> statuses = ArgumentCaptor.forClass(TaskStatus.class);
            verify(watcher, times(2)).taskAdded(statuses.capture());
            assertThat(statuses.getAllValues())
                .extracting(s -> s.info().id())
                .containsExactly(existingTaskOnOther.get(), newTaskOnOther.get());

            verifyNoMoreInteractions(watcher);
        }
    }
}
