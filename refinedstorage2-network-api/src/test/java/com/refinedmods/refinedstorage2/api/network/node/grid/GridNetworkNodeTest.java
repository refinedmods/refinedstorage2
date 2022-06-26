package com.refinedmods.refinedstorage2.api.network.node.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.network.test.extension.AddNetworkNode;
import com.refinedmods.refinedstorage2.api.network.test.extension.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.api.network.test.extension.NetworkTestExtension;
import com.refinedmods.refinedstorage2.api.network.test.extension.SetupNetwork;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
@ExtendWith(NetworkTestExtension.class)
@SetupNetwork
class GridNetworkNodeTest {
    @AddNetworkNode
    GridNetworkNode<String> sut;

    @BeforeEach
    void setUp(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        networkStorage.addSource(new TrackedStorageImpl<>(new LimitedStorageImpl<>(1000), () -> 0L));
        networkStorage.insert("A", 100, Action.EXECUTE, EmptySource.INSTANCE);
        networkStorage.insert("B", 200, Action.EXECUTE, EmptySource.INSTANCE);
    }

    @Test
    void Test_resource_count() {
        // Act
        long count = sut.getResourceCount();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void Test_iterating_through_resources() {
        // Arrange
        List<ResourceAmount<String>> resourceAmounts = new ArrayList<>();
        List<Optional<TrackedResource>> trackedResources = new ArrayList<>();

        // Act
        sut.forEachResource((resourceAmount, trackedResource) -> {
            resourceAmounts.add(resourceAmount);
            trackedResources.add(trackedResource);
        }, EmptySource.class);

        // Assert
        assertThat(resourceAmounts).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 100),
                new ResourceAmount<>("B", 200)
        );
        assertThat(trackedResources).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                Optional.of(new TrackedResource(EmptySource.INSTANCE.getName(), 0L)),
                Optional.of(new TrackedResource(EmptySource.INSTANCE.getName(), 0L))
        );
    }

    @Test
    void Test_should_notify_watchers() {
        // Arrange
        FakeGridWatcher watcher = new FakeGridWatcher();

        // Act
        sut.addWatcher(watcher);
        sut.onActiveChanged(true);
        sut.onActiveChanged(false);
        sut.removeWatcher(watcher);
        sut.onActiveChanged(true);
        sut.onActiveChanged(false);

        // Assert
        assertThat(watcher.changes).containsExactly(true, false);
    }

    private static class FakeGridWatcher implements GridWatcher {
        private final List<Boolean> changes = new ArrayList<>();

        @Override
        public void onActiveChanged(boolean active) {
            changes.add(active);
        }
    }
}
