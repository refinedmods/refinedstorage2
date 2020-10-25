package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.network.node.FakeNetworkNodeAdapter;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.StubNetworkNodeReference;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RefinedStorage2Test
class NetworkManagerImplTest {
    @Test
    void Test_notifying_network_manager_of_node_being_added_while_node_not_present_should_fail() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        NetworkManager networkManager = new NetworkManagerImpl(nodeAdapter);

        // Act
        Executable action = () -> networkManager.onNodeAdded(BlockPos.ORIGIN);

        // Assert
        NetworkManagerException e = assertThrows(NetworkManagerException.class, action);
        assertThat(e.getMessage()).isEqualTo("Could not find added node at position BlockPos{x=0, y=0, z=0}");
    }

    @Test
    void Test_adding_node_should_form_network() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        NetworkManager networkManager = new NetworkManagerImpl(nodeAdapter);

        // Act
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        Network network01 = networkManager.onNodeAdded(BlockPos.ORIGIN);

        // Assert
        assertThat(network01.getNodeReferences()).containsExactlyInAnyOrder(
                new StubNetworkNodeReference(node01)
        );
        assertThat(node01.getNetwork()).isSameAs(network01);
        assertThat(networkManager.getNetworks()).hasSize(1);
    }

    @Test
    void Test_when_adding_node_having_a_neighboring_node_without_network_should_fail() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        NetworkManager networkManager = new NetworkManagerImpl(nodeAdapter);

        // Act
        nodeAdapter.setNode(BlockPos.ORIGIN);
        nodeAdapter.setNode(BlockPos.ORIGIN.down());

        Executable action = () -> networkManager.onNodeAdded(BlockPos.ORIGIN);

        // Assert
        NetworkManagerException e = assertThrows(NetworkManagerException.class, action);
        assertThat(e.getMessage()).isEqualTo("The network manager was left in an invalid state. Network node at BlockPos{x=0, y=-1, z=0} has no network!");
    }

    @Test
    void Test_adding_node_should_join_existing_network() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        NetworkManager networkManager = new NetworkManagerImpl(nodeAdapter);

        // Act & assert
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        Network network01 = networkManager.onNodeAdded(BlockPos.ORIGIN);

        assertThat(network01.getNodeReferences()).containsExactlyInAnyOrder(
                new StubNetworkNodeReference(node01)
        );
        assertThat(node01.getNetwork()).isSameAs(network01);

        NetworkNode node02 = nodeAdapter.setNode(BlockPos.ORIGIN.down());
        Network network02 = networkManager.onNodeAdded(BlockPos.ORIGIN.down());

        assertThat(network02).isSameAs(network01);
        assertThat(node02.getNetwork()).isSameAs(network02);
        assertThat(network02.getNodeReferences()).containsExactlyInAnyOrder(
                new StubNetworkNodeReference(node01),
                new StubNetworkNodeReference(node02)
        );
    }

    @Test
    void Test_adding_a_node_should_merge_existing_networks() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        NetworkManager networkManager = new NetworkManagerImpl(nodeAdapter);

        // Act & assert
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        Network network01 = networkManager.onNodeAdded(BlockPos.ORIGIN);

        assertThat(network01.getNodeReferences()).containsExactlyInAnyOrder(
                new StubNetworkNodeReference(node01)
        );
        assertThat(node01.getNetwork()).isSameAs(network01);
        assertThat(networkManager.getNetworks()).hasSize(1);

        NetworkNode node02 = nodeAdapter.setNode(BlockPos.ORIGIN.down().down());
        Network network02 = networkManager.onNodeAdded(BlockPos.ORIGIN.down().down());

        assertThat(network02.getNodeReferences()).containsExactlyInAnyOrder(
                new StubNetworkNodeReference(node02)
        );
        assertThat(node02.getNetwork()).isSameAs(network02);
        assertThat(networkManager.getNetworks()).hasSize(2);

        NetworkNode node03 = nodeAdapter.setNode(BlockPos.ORIGIN.down());
        Network network03 = networkManager.onNodeAdded(BlockPos.ORIGIN.down());

        assertThat(network03.getNodeReferences()).containsExactlyInAnyOrder(
                new StubNetworkNodeReference(node01),
                new StubNetworkNodeReference(node02),
                new StubNetworkNodeReference(node03)
        );
        assertThat(node01.getNetwork()).isSameAs(network03);
        assertThat(node02.getNetwork()).isSameAs(network03);
        assertThat(node03.getNetwork()).isSameAs(network03);
        assertThat(networkManager.getNetworks()).hasSize(1);
    }

    @Test
    void Test_splitting_networks_in_two_networks() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        NetworkManager networkManager = new NetworkManagerImpl(nodeAdapter);

        // Act & assert
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        networkManager.onNodeAdded(BlockPos.ORIGIN);

        NetworkNode node02 = nodeAdapter.setNode(BlockPos.ORIGIN.down());
        networkManager.onNodeAdded(BlockPos.ORIGIN.down());

        NetworkNode node03 = nodeAdapter.setNode(BlockPos.ORIGIN.down().down());
        networkManager.onNodeAdded(BlockPos.ORIGIN.down().down());

        assertThat(networkManager.getNetworks()).hasSize(1);

        nodeAdapter.removeNode(BlockPos.ORIGIN.down());
        networkManager.onNodeRemoved(node02);

        assertThat(networkManager.getNetworks()).hasSize(2);
        assertThat(networkManager.getNetworks()).anySatisfy(network -> assertThat(network.getNodeReferences()).containsExactly(new StubNetworkNodeReference(node01)));
        assertThat(networkManager.getNetworks()).anySatisfy(network -> assertThat(network.getNodeReferences()).containsExactly(new StubNetworkNodeReference(node03)));
    }

    @Test
    void Test_splitting_networks_in_three_networks() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        NetworkManager networkManager = new NetworkManagerImpl(nodeAdapter);

        // Act & assert
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        networkManager.onNodeAdded(BlockPos.ORIGIN);

        NetworkNode node02 = nodeAdapter.setNode(BlockPos.ORIGIN.north());
        networkManager.onNodeAdded(BlockPos.ORIGIN.north());

        NetworkNode node03 = nodeAdapter.setNode(BlockPos.ORIGIN.east());
        networkManager.onNodeAdded(BlockPos.ORIGIN.east());

        NetworkNode node04 = nodeAdapter.setNode(BlockPos.ORIGIN.up());
        networkManager.onNodeAdded(BlockPos.ORIGIN.up());

        assertThat(networkManager.getNetworks()).hasSize(1);

        nodeAdapter.removeNode(BlockPos.ORIGIN);
        networkManager.onNodeRemoved(node01);

        assertThat(networkManager.getNetworks()).hasSize(3);
        assertThat(networkManager.getNetworks()).anySatisfy(network -> assertThat(network.getNodeReferences()).containsExactly(new StubNetworkNodeReference(node02)));
        assertThat(networkManager.getNetworks()).anySatisfy(network -> assertThat(network.getNodeReferences()).containsExactly(new StubNetworkNodeReference(node03)));
        assertThat(networkManager.getNetworks()).anySatisfy(network -> assertThat(network.getNodeReferences()).containsExactly(new StubNetworkNodeReference(node04)));
    }

    @Test
    void Test_when_removing_a_node_having_a_neighboring_node_without_network_should_fail() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        NetworkManager networkManager = new NetworkManagerImpl(nodeAdapter);

        // Act & assert
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        networkManager.onNodeAdded(BlockPos.ORIGIN);

        NetworkNode node02 = nodeAdapter.setNode(BlockPos.ORIGIN.down());
        networkManager.onNodeAdded(BlockPos.ORIGIN.down());

        assertThat(node02.getNetwork()).isNotNull();

        node02.setNetwork(null);

        NetworkManagerException e = assertThrows(NetworkManagerException.class, () -> networkManager.onNodeRemoved(node01));
        assertThat(e.getMessage()).isEqualTo("The network manager was left in an invalid state. Network node at BlockPos{x=0, y=-1, z=0} has no network!");
    }

    @Test
    void Test_removing_last_node_removes_network() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        NetworkManager networkManager = new NetworkManagerImpl(nodeAdapter);

        // Act & assert
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        networkManager.onNodeAdded(BlockPos.ORIGIN);

        NetworkNode node02 = nodeAdapter.setNode(BlockPos.ORIGIN.down());
        networkManager.onNodeAdded(BlockPos.ORIGIN.down());

        assertThat(networkManager.getNetworks()).hasSize(1);

        nodeAdapter.removeNode(BlockPos.ORIGIN);
        networkManager.onNodeRemoved(node01);
        assertThat(networkManager.getNetworks()).hasSize(1);

        nodeAdapter.removeNode(BlockPos.ORIGIN.down());
        networkManager.onNodeRemoved(node02);
        assertThat(networkManager.getNetworks()).isEmpty();
    }

    @Test
    void Test_removing_a_node_with_unmatched_neighbor_network_should_fail() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        NetworkManager networkManager = new NetworkManagerImpl(nodeAdapter);

        // Act & assert
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        networkManager.onNodeAdded(BlockPos.ORIGIN);

        NetworkNode node02 = nodeAdapter.setNode(BlockPos.ORIGIN.down());
        networkManager.onNodeAdded(BlockPos.ORIGIN.down());

        node02.setNetwork(new NetworkImpl(UUID.randomUUID()));

        NetworkManagerException e = assertThrows(NetworkManagerException.class, () -> networkManager.onNodeRemoved(node01));
        assertThat(e.getMessage()).isEqualTo("The network manager was left in invalid state. The network of a neighboring node doesn't match the origin node. The origin node is located at BlockPos{x=0, y=0, z=0}");
    }

    @Test
    void Test_removing_a_node_with_the_node_still_existing_should_fail() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        NetworkManager networkManager = new NetworkManagerImpl(nodeAdapter);

        // Act & assert
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        networkManager.onNodeAdded(BlockPos.ORIGIN);

        nodeAdapter.setNode(BlockPos.ORIGIN.down());
        networkManager.onNodeAdded(BlockPos.ORIGIN.down());

        NetworkManagerException e = assertThrows(NetworkManagerException.class, () -> networkManager.onNodeRemoved(node01));
        assertThat(e.getMessage()).isEqualTo("The removed node at BlockPos{x=0, y=0, z=0} is still present in the world!");
    }
}
