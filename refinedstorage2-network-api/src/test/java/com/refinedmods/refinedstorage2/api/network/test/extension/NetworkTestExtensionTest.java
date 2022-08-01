package com.refinedmods.refinedstorage2.api.network.test.extension;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.test.NetworkTestFixtures;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({NetworkTestExtension.class})
@SetupNetwork(id = "a", energyCapacity = 100, energyStored = 50)
@SetupNetwork(id = "b")
class NetworkTestExtensionTest {
    @InjectNetwork("a")
    Network a;

    @InjectNetwork("b")
    Network b;

    @AddNetworkNode(networkId = "a")
    StorageNetworkNode<String> storageInA;

    @AddDiskDrive(networkId = "b", active = false)
    DiskDriveNetworkNode storageInB;

    @Test
    void shouldInjectNetwork() {
        // Assert
        assertThat(a).isNotNull();
        assertThat(b).isNotNull();
        assertThat(a).isNotSameAs(b);
    }

    @Test
    void shouldSetActivenessOfNetworkNode() {
        // Assert
        assertThat(storageInA.isActive()).isTrue();
        assertThat(storageInB.isActive()).isFalse();
    }

    @Test
    void shouldSetEnergyComponent() {
        // Arrange
        final EnergyNetworkComponent energy = a.getComponent(EnergyNetworkComponent.class);

        // Assert
        assertThat(energy.getCapacity()).isEqualTo(100L);
        assertThat(energy.getStored()).isEqualTo(50L);
    }

    @Test
    void shouldLoadNetworkNode() {
        // Assert
        assertThat(storageInA).isNotNull();
        assertThat(storageInB).isNotNull();
    }

    @Test
    void shouldAddNetworkNodeToGraph() {
        // Assert
        assertThat(a.getComponent(GraphNetworkComponent.class).getContainers())
            .extracting(NetworkNodeContainer::getNode)
            .containsExactly(storageInA);

        assertThat(b.getComponent(GraphNetworkComponent.class).getContainers())
            .extracting(NetworkNodeContainer::getNode)
            .containsExactly(storageInB);
    }

    @Test
    void shouldInjectStorageChannel(
        @InjectNetworkStorageChannel(networkId = "a") final StorageChannel<String> storageChannelA,
        @InjectNetworkStorageChannel(networkId = "b") final StorageChannel<String> storageChannelB) {
        // Assert
        assertThat(storageChannelA).isSameAs(
            a.getComponent(StorageNetworkComponent.class).getStorageChannel(NetworkTestFixtures.STORAGE_CHANNEL_TYPE));
        assertThat(storageChannelB).isSameAs(
            b.getComponent(StorageNetworkComponent.class).getStorageChannel(NetworkTestFixtures.STORAGE_CHANNEL_TYPE));
    }

    @Test
    void shouldInjectNetworkEnergyComponent(
        @InjectNetworkEnergyComponent(networkId = "a") final EnergyNetworkComponent networkEnergyA,
        @InjectNetworkEnergyComponent(networkId = "b") final EnergyNetworkComponent networkEnergyB) {
        // Assert
        assertThat(networkEnergyA).isSameAs(a.getComponent(EnergyNetworkComponent.class));
        assertThat(networkEnergyB).isSameAs(b.getComponent(EnergyNetworkComponent.class));
    }

    @Nested
    @SetupNetwork(id = "c")
    class NestedTest {
        @InjectNetwork("c")
        Network nestedNetwork;

        @AddNetworkNode(networkId = "a")
        StorageNetworkNode<String> nodeInA;

        @Test
        void testNestedNetworkAndNestedNetworkNode() {
            assertThat(nestedNetwork).isNotNull();
            assertThat(nodeInA).isNotNull();
            assertThat(a.getComponent(GraphNetworkComponent.class).getContainers())
                .extracting(NetworkNodeContainer::getNode)
                .containsExactlyInAnyOrder(storageInA, nodeInA);
        }
    }
}
