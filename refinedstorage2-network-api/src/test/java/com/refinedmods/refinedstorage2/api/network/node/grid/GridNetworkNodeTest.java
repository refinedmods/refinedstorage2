package com.refinedmods.refinedstorage2.api.network.node.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.network.node.storage.FakeActor;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;
import com.refinedmods.refinedstorage2.network.test.nodefactory.AbstractNetworkNodeFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@NetworkTest
@SetupNetwork
class GridNetworkNodeTest {
    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = AbstractNetworkNodeFactory.PROPERTY_ENERGY_USAGE, longValue = 5)
    })
    GridNetworkNode<String> sut;

    @BeforeEach
    void setUp(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        networkStorage.addSource(new TrackedStorageImpl<>(new LimitedStorageImpl<>(1000), () -> 2L));
        networkStorage.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert("B", 200, Action.EXECUTE, EmptyActor.INSTANCE);
    }

    @Test
    void testInitialState() {
        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(5);
    }

    @Test
    void testResourceAmount() {
        // Act
        final long count = sut.getResourceAmount();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testIteratingThroughResources() {
        // Arrange
        final List<ResourceAmount<String>> resourceAmounts = new ArrayList<>();
        final List<Optional<TrackedResource>> trackedResources = new ArrayList<>();

        // Act
        sut.forEachResource((resourceAmount, trackedResource) -> {
            resourceAmounts.add(resourceAmount);
            trackedResources.add(trackedResource);
        }, EmptyActor.class);

        // Assert
        assertThat(resourceAmounts).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 100),
            new ResourceAmount<>("B", 200)
        );
        assertThat(trackedResources).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            Optional.of(new TrackedResource("Empty", 2L)),
            Optional.of(new TrackedResource("Empty", 2L))
        );
    }

    @Test
    void shouldNotifyWatchersOfActivenessChanges() {
        // Arrange
        final FakeGridWatcher watcher = new FakeGridWatcher();

        // Act
        sut.addWatcher(watcher, EmptyActor.class);
        sut.setActive(true);
        sut.setActive(false);
        sut.removeWatcher(watcher);
        sut.setActive(true);
        sut.setActive(false);

        // Assert
        assertThat(watcher.activenessChanges).containsExactly(true, false);
        assertThat(watcher.changes).isEmpty();
    }

    @Test
    void shouldNotifyWatchersOfStorageChanges(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final FakeGridWatcher watcher = new FakeGridWatcher();
        sut.addWatcher(watcher, FakeActor.class);

        // Act
        networkStorage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert("A", 1, Action.EXECUTE, FakeActor.INSTANCE);
        sut.removeWatcher(watcher);
        networkStorage.insert("A", 1, Action.EXECUTE, FakeActor.INSTANCE);

        // Assert
        assertThat(watcher.activenessChanges).isEmpty();
        assertThat(watcher.changes).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new Change<>("A", 10, null),
            new Change<>("A", 1, new TrackedResource("Fake", 2))
        );
    }

    @Test
    void shouldNotBeAbleToRemoveUnknownWatcher() {
        // Arrange
        final FakeGridWatcher watcher = new FakeGridWatcher();

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.removeWatcher(watcher));
    }

    @Test
    void shouldNotBeAbleToAddDuplicateWatcher() {
        // Arrange
        final FakeGridWatcher watcher = new FakeGridWatcher();
        final Class<? extends Actor> actorType1 = EmptyActor.class;
        final Class<? extends Actor> actorType2 = FakeActor.class;

        sut.addWatcher(watcher, actorType1);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.addWatcher(watcher, actorType1));
        assertThrows(IllegalArgumentException.class, () -> sut.addWatcher(watcher, actorType2));
    }

    @Test
    void shouldCreateService(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final GridService<String> service = sut.createService(
            FakeActor.INSTANCE,
            r -> 5L,
            1
        );

        final InMemoryStorageImpl<String> source = new InMemoryStorageImpl<>();
        source.insert("Z", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        service.insert("Z", GridInsertMode.SINGLE_RESOURCE, source);
        final Collection<ResourceAmount<String>> afterSingle = networkStorage
            .getAll()
            .stream()
            .map(ra -> new ResourceAmount<>(ra.getResource(), ra.getAmount()))
            .toList();

        service.insert("Z", GridInsertMode.ENTIRE_RESOURCE, source);
        final Collection<ResourceAmount<String>> afterEntire = networkStorage
            .getAll()
            .stream()
            .map(ra -> new ResourceAmount<>(ra.getResource(), ra.getAmount()))
            .toList();

        // Assert
        assertThat(afterSingle).usingRecursiveFieldByFieldElementComparator().contains(
            new ResourceAmount<>("Z", 1)
        );
        assertThat(afterEntire).usingRecursiveFieldByFieldElementComparator().contains(
            new ResourceAmount<>("Z", 6)
        );
    }

    private static class FakeGridWatcher implements GridWatcher<String> {
        private final List<Boolean> activenessChanges = new ArrayList<>();
        private final List<Change<String>> changes = new ArrayList<>();

        @Override
        public void onActiveChanged(final boolean newActive) {
            activenessChanges.add(newActive);
        }

        @Override
        public void onChanged(final ResourceListOperationResult<String> change,
                              final @Nullable TrackedResource trackedResource) {
            changes.add(new Change<>(
                change.resourceAmount().getResource(),
                change.change(),
                trackedResource
            ));
        }
    }

    private record Change<T>(T resource, long change, @Nullable TrackedResource trackedResource) {
    }
}
