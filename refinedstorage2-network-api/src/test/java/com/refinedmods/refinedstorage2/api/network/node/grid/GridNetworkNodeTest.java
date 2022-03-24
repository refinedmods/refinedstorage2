package com.refinedmods.refinedstorage2.api.network.node.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.NetworkUtil;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.test.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
public class GridNetworkNodeTest {
    private GridNetworkNode<String> sut;

    @BeforeEach
    void setUp() {
        sut = new GridNetworkNode<>(10, StorageChannelTypes.FAKE);

        Network network = NetworkUtil.create();
        sut.setNetwork(network);
        network.addContainer(() -> sut);

        StorageChannel<String> fakeStorageChannel = network.getComponent(StorageNetworkComponent.class)
                .getStorageChannel(StorageChannelTypes.FAKE);

        fakeStorageChannel.addSource(new CappedStorage<>(1000));
        fakeStorageChannel.insert("A", 100, () -> "Test");
        fakeStorageChannel.insert("B", 200, Action.EXECUTE, EmptySource.INSTANCE);
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
        List<Optional<StorageTracker.Entry>> trackerEntries = new ArrayList<>();

        // Act
        sut.forEachResource((resourceAmount, entry) -> {
            resourceAmounts.add(resourceAmount);
            trackerEntries.add(entry);
        });

        // Assert
        assertThat(resourceAmounts).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 100),
                new ResourceAmount<>("B", 200)
        );
        assertThat(trackerEntries)
                .filteredOn(Optional::isPresent)
                .map(o -> o.get().name())
                .containsExactly("Test");
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
