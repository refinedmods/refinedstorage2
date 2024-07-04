package com.refinedmods.refinedstorage.api.network.impl;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.grid.watcher.GridWatcher;
import com.refinedmods.refinedstorage.api.network.ConnectionProvider;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.container.NetworkNodeContainerPriorities;
import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.EmptyActor;
import com.refinedmods.refinedstorage.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage.network.test.fake.FakeActor;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static com.refinedmods.refinedstorage.api.network.impl.PriorityNetworkBuilderImplTest.MasterSlave.MASTER;
import static com.refinedmods.refinedstorage.api.network.impl.PriorityNetworkBuilderImplTest.MasterSlave.SLAVE;
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
        final NetworkSide master = createNetworkSide(MASTER, () -> originalNetwork);
        final NetworkNodeContainer connector = createContainerWithNetwork(container -> originalNetwork);
        final NetworkSide slave = createNetworkSide(SLAVE, () -> originalNetwork);
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
            SLAVE,
            10L,
            null
        );
        verifyNoMoreInteractions(slave.watcher);

        verify(master.watcher, times(1)).onChanged(
            SLAVE,
            -10L,
            null
        );
        verifyNoMoreInteractions(master.watcher);
    }

    @Test
    void shouldRespectPriorityWhenMerging() {
        // Arrange
        final NetworkSide master = createNetworkSide(MASTER, () -> new NetworkImpl(componentMapFactory));
        final NetworkNodeContainer connector = createContainer();
        final NetworkSide slave = createNetworkSide(SLAVE, () -> new NetworkImpl(componentMapFactory));

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

        verify(slave.watcher, times(1)).invalidate();
        verify(slave.watcher).onChanged(
            SLAVE,
            10L,
            null
        );
        verify(slave.watcher).onChanged(
            MASTER,
            10L,
            null
        );
        verifyNoMoreInteractions(slave.watcher);

        verify(master.watcher, times(1)).onChanged(
            SLAVE,
            10L,
            null
        );
        verifyNoMoreInteractions(master.watcher);
    }

    private NetworkSide createNetworkSide(final MasterSlave side,
                                          final Supplier<Network> networkFactory) {
        final StorageNetworkNode nodeA = new StorageNetworkNode(0, 0, 1);
        final InMemoryStorageImpl storage = new InMemoryStorageImpl();
        storage.insert(side, 10, Action.EXECUTE, FakeActor.INSTANCE);
        nodeA.setProvider(index -> Optional.of(storage));
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
        final GridWatcher watcher = mock(GridWatcher.class, "watcher for " + side.name());
        nodeB.setActive(true);
        nodeB.addWatcher(watcher, EmptyActor.class);
        return new NetworkSide(a, nodeA, b, nodeB, watcher);
    }

    private record NetworkSide(
        NetworkNodeContainer a,
        StorageNetworkNode nodeA,
        NetworkNodeContainer b,
        GridNetworkNode nodeB,
        GridWatcher watcher
    ) {
    }

    protected enum MasterSlave implements ResourceKey {
        MASTER, SLAVE
    }
}
