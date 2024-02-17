package com.refinedmods.refinedstorage2.api.network.impl;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.watcher.GridWatcher;
import com.refinedmods.refinedstorage2.api.network.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.impl.node.container.NetworkNodeContainerPriorities;
import com.refinedmods.refinedstorage2.api.network.impl.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;
import com.refinedmods.refinedstorage2.network.test.util.FakeActor;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class PriorityNetworkBuilderImplTest extends AbstractNetworkBuilderImplTest {
    @Test
    void shouldRespectPriorityWhenSplitting() {
        // Arrange
        final Network originalNetwork = new NetworkImpl(componentMapFactory);
        final NetworkSide master = createNetworkSide("master", () -> originalNetwork);
        final NetworkNodeContainer connector = createContainerWithNetwork(container -> originalNetwork);
        final NetworkSide slave = createNetworkSide("slave", () -> originalNetwork);
        clearInvocations(master.watcher);

        final ConnectionProvider connectionProvider = new FakeConnectionProvider()
            .with(master.a, master.b, slave.a, slave.b)
            .connect(master.a, master.b)
            .connect(slave.a, slave.b);

        // Act
        sut.remove(connector, connectionProvider);

        // Assert
        assertThat(master.nodeA.getNetwork()).isSameAs(master.nodeB.getNetwork());
        assertThat(slave.nodeA.getNetwork()).isSameAs(slave.nodeB.getNetwork());

        assertThat(slave.nodeA.getNetwork()).isNotSameAs(master.nodeA.getNetwork());
        assertThat(slave.nodeB.getNetwork()).isNotSameAs(master.nodeA.getNetwork());

        final InOrder inOrder = inOrder(slave.watcher);
        inOrder.verify(slave.watcher, times(1)).invalidate();
        inOrder.verify(slave.watcher, times(1)).onChanged(
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            "slave",
            10L,
            null
        );
        verifyNoMoreInteractions(slave.watcher);

        verify(master.watcher, times(1)).onChanged(
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            "slave",
            -10L,
            null
        );
        verifyNoMoreInteractions(master.watcher);
    }

    @Test
    void shouldRespectPriorityWhenMerging() {
        // Arrange
        final NetworkSide master = createNetworkSide("master", () -> new NetworkImpl(componentMapFactory));
        final NetworkNodeContainer connector = createContainer();
        final NetworkSide slave = createNetworkSide("slave", () -> new NetworkImpl(componentMapFactory));

        final ConnectionProvider connectionProvider = new FakeConnectionProvider()
            .with(master.a, master.b, connector, slave.a, slave.b)
            .connect(master.a, master.b)
            .connect(master.b, connector)
            // <->
            .connect(connector, slave.a)
            .connect(slave.a, slave.b);

        // Act
        sut.initialize(connector, connectionProvider);

        // Assert
        assertThat(slave.nodeA.getNetwork()).isSameAs(master.nodeA.getNetwork());
        assertThat(slave.nodeB.getNetwork()).isSameAs(master.nodeA.getNetwork());

        final InOrder inOrder = inOrder(slave.watcher);
        inOrder.verify(slave.watcher, times(1)).invalidate();
        inOrder.verify(slave.watcher).onChanged(
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            "slave",
            10L,
            null
        );
        inOrder.verify(slave.watcher).onChanged(
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            "master",
            10L,
            null
        );
        inOrder.verifyNoMoreInteractions();

        verify(master.watcher, times(1)).onChanged(
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            "slave",
            10L,
            null
        );
        verifyNoMoreInteractions(master.watcher);
    }

    private NetworkSide createNetworkSide(final String name, final Supplier<Network> networkFactory) {
        final StorageNetworkNode<String> nodeA = new StorageNetworkNode<>(
            0,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE
        );
        final InMemoryStorageImpl<String> storage = new InMemoryStorageImpl<>();
        storage.insert(name, 10, Action.EXECUTE, FakeActor.INSTANCE);
        nodeA.setStorage(storage);
        final NetworkNodeContainer a = createContainerWithNetwork(
            nodeA,
            container -> networkFactory.get(),
            0
        );
        nodeA.setActive(true);
        final GridNetworkNode nodeB = new GridNetworkNode(0);
        final NetworkNodeContainer b = createContainerWithNetwork(
            nodeB,
            container -> a.getNode().getNetwork(),
            NetworkNodeContainerPriorities.GRID
        );
        final GridWatcher watcher = mock(GridWatcher.class, "watcher for " + name);
        nodeB.setActive(true);
        nodeB.addWatcher(watcher, EmptyActor.class);
        return new NetworkSide(a, nodeA, b, nodeB, watcher);
    }

    private record NetworkSide(
        NetworkNodeContainer a,
        StorageNetworkNode<String> nodeA,
        NetworkNodeContainer b,
        GridNetworkNode nodeB,
        GridWatcher watcher
    ) {
    }
}
