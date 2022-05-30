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
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
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

    @AddDiskDrive(networkId = "b")
    DiskDriveNetworkNode storageInB;

    @Test
    void Test_should_inject_network() {
        // Assert
        assertThat(a).isNotNull();
        assertThat(b).isNotNull();
        assertThat(a).isNotSameAs(b);
    }

    @Test
    void Test_should_set_energy() {
        // Arrange
        EnergyNetworkComponent energy = a.getComponent(EnergyNetworkComponent.class);

        // Assert
        assertThat(energy.getCapacity()).isEqualTo(100L);
        assertThat(energy.getStored()).isEqualTo(50L);
    }

    @Test
    void Test_should_set_network_node() {
        // Assert
        assertThat(storageInA).isNotNull();
        assertThat(storageInB).isNotNull();
    }

    @Test
    void Test_should_add_network_node_to_graph() {
        // Assert
        assertThat(a.getComponent(GraphNetworkComponent.class).getContainers())
                .extracting(NetworkNodeContainer::getNode)
                .containsExactly(storageInA);

        assertThat(b.getComponent(GraphNetworkComponent.class).getContainers())
                .extracting(NetworkNodeContainer::getNode)
                .containsExactly(storageInB);
    }

    @Test
    void Test_should_inject_storage_channel(@InjectNetworkStorageChannel(networkId = "a") StorageChannel<String> storageChannelA,
                                            @InjectNetworkStorageChannel(networkId = "b") StorageChannel<String> storageChannelB) {
        // Assert
        assertThat(storageChannelA).isSameAs(a.getComponent(StorageNetworkComponent.class).getStorageChannel(NetworkTestFixtures.STORAGE_CHANNEL_TYPE));
        assertThat(storageChannelB).isSameAs(b.getComponent(StorageNetworkComponent.class).getStorageChannel(NetworkTestFixtures.STORAGE_CHANNEL_TYPE));
    }

    @Test
    void Test_should_inject_network_energy_component(@InjectNetworkEnergyComponent(networkId = "a") EnergyNetworkComponent networkEnergyA,
                                                     @InjectNetworkEnergyComponent(networkId = "b") EnergyNetworkComponent networkEnergyB) {
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
        void test() {
            assertThat(nestedNetwork).isNotNull();
            assertThat(nodeInA).isNotNull();
            assertThat(a.getComponent(GraphNetworkComponent.class).getContainers())
                    .extracting(NetworkNodeContainer::getNode)
                    .containsExactlyInAnyOrder(storageInA, nodeInA);
        }
    }
}
