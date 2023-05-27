package com.refinedmods.refinedstorage2.api.network.impl;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PriorityNetworkBuilderImplTest extends AbstractNetworkBuilderImplTest {
    @Test
    void shouldRespectPriorityWhenSplitting() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        final NetworkNodeContainer container1 = createContainerWithNetwork();
        final Network originalNetwork = container1.getNode().getNetwork();
        final NetworkNodeContainer container2 = createContainerWithNetwork(
            container -> container1.getNode().getNetwork()
        );
        final StorageNetworkNode<String> node3 = new StorageNetworkNode<>(0, NetworkTestFixtures.STORAGE_CHANNEL_TYPE);
        final InMemoryStorageImpl<String> storage = new InMemoryStorageImpl<>();
        storage.insert("N3", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        node3.setStorage(storage);
        final NetworkNodeContainer container3 = createContainerWithNetwork(
            node3,
            container -> container1.getNode().getNetwork(),
            0
        );
        node3.setActive(true);
        final GridNetworkNode node4 = new GridNetworkNode(0, NetworkTestFixtures.STORAGE_CHANNEL_TYPES);
        final NetworkNodeContainer container4 = createContainerWithNetwork(
            node4,
            container -> container1.getNode().getNetwork(),
            Integer.MAX_VALUE
        );
        final GridWatcher watcher = mock(GridWatcher.class);
        node4.addWatcher(watcher, EmptyActor.class);

        connectionProvider
            .with(container1, container2, container3, container4)
            .connect(container4, container3);

        // Act
        sut.remove(container2, connectionProvider);

        // Assert
        // Container 1 retains its network.
        assertThat(container1.getNode().getNetwork())
            .isNotNull()
            .isSameAs(originalNetwork);
        assertThat(container1.getNode().getNetwork().getComponent(StorageNetworkComponent.class)
            .getStorageChannel(NetworkTestFixtures.STORAGE_CHANNEL_TYPE).getAll()).isEmpty();

        // Container 2 has been removed.
        assertThat(container2.getNode().getNetwork()).isNull();

        // Container 3 and 4 get a new network.
        assertThat(container3.getNode().getNetwork())
            .isNotNull()
            .isNotSameAs(originalNetwork)
            .isSameAs(container4.getNode().getNetwork());
        assertThat(container3.getNode().getNetwork().getComponent(StorageNetworkComponent.class)
            .getStorageChannel(NetworkTestFixtures.STORAGE_CHANNEL_TYPE).getAll()).hasSize(1);

        // Here we ensure that container 4 (the grid) is initialized *after* container 3 (the storage),
        // according to the priority declared above.
        verify(watcher, times(1)).onNetworkChanged();
        verify(watcher).onChanged(
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            "N3",
            10L,
            null
        );
    }
}
