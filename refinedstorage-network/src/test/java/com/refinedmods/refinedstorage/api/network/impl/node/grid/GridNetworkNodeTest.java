package com.refinedmods.refinedstorage.api.network.impl.node.grid;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
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
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;
import com.refinedmods.refinedstorage.network.test.fixtures.ActorFixture;
import com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
    void shouldNotifyWatchersOfStorageChanges(
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
    void shouldBeAbleToAddWatcherWithoutNetworkAndRemoveItIfTheNetworkBecameAvailableAfter() {
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

        // Assert
        verify(watcher).onActiveChanged(true);
        verify(watcher).invalidate();
        verify(watcher).onChanged(
            eq(A),
            eq(100L),
            any()
        );
        verify(watcher).onChanged(
            eq(B),
            eq(200L),
            any()
        );
        verifyNoMoreInteractions(watcher);
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

    @Test
    void shouldDetachWatchersFromOldNetworkAndReattachToNewNetwork(
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
