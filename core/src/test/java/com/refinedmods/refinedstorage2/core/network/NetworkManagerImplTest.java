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
    private final NetworkManager networkManager = new NetworkManagerImpl();

    @Test
    void Test_notifying_network_manager_of_node_being_added_while_node_not_present_should_fail() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        // Act
        Executable action = () -> networkManager.onNodeAdded(nodeAdapter, BlockPos.ORIGIN);

        // Assert
        NetworkManagerException e = assertThrows(NetworkManagerException.class, action);
        assertThat(e.getMessage()).isEqualTo("Could not find added node at position BlockPos{x=0, y=0, z=0}");
    }

    @Test
    void Test_adding_node_should_form_network() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        // Act
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        Network network01 = networkManager.onNodeAdded(nodeAdapter, node01.getPosition());

        // Assert
        assertThat(networkManager.getNetworks()).hasSize(1);
        assertThat(networkManager.getNetworks()).anySatisfy(network -> {
            assertThat(network.getNodeReferences()).containsExactly(new StubNetworkNodeReference(node01));
            assertThat(network01).isSameAs(network);
            assertThat(node01.getNetwork()).isSameAs(network);
        });
    }

    @Test
    void Test_when_adding_node_having_a_neighboring_node_without_network_should_fail() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        // Act
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        nodeAdapter.setNode(BlockPos.ORIGIN.down());

        Executable action = () -> networkManager.onNodeAdded(nodeAdapter, node01.getPosition());

        // Assert
        NetworkManagerException e = assertThrows(NetworkManagerException.class, action);
        assertThat(e.getMessage()).isEqualTo("The network manager was left in an invalid state. Network node at BlockPos{x=0, y=-1, z=0} has no network!");
    }

    @Test
    void Test_adding_node_should_join_existing_network() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        // Act
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        networkManager.onNodeAdded(nodeAdapter, node01.getPosition());

        NetworkNode node02 = nodeAdapter.setNode(BlockPos.ORIGIN.down());
        Network network02 = networkManager.onNodeAdded(nodeAdapter, node02.getPosition());

        // Assert
        assertThat(networkManager.getNetworks()).hasSize(1);
        assertThat(networkManager.getNetworks()).anySatisfy(network -> {
            assertThat(network.getNodeReferences()).containsExactlyInAnyOrder(
                    new StubNetworkNodeReference(node01),
                    new StubNetworkNodeReference(node02)
            );
            assertThat(network02).isSameAs(network);
            assertThat(node01.getNetwork()).isSameAs(network);
            assertThat(node02.getNetwork()).isSameAs(network);
        });
    }

    @Test
    void Test_adding_a_node_should_merge_existing_networks() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        // Act & assert
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        Network network01 = networkManager.onNodeAdded(nodeAdapter, node01.getPosition());

        assertThat(networkManager.getNetworks()).hasSize(1);
        assertThat(networkManager.getNetworks()).anySatisfy(network -> {
            assertThat(network.getNodeReferences()).containsExactly(new StubNetworkNodeReference(node01));
            assertThat(network01).isSameAs(network);
            assertThat(node01.getNetwork()).isSameAs(network);
        });

        NetworkNode node02 = nodeAdapter.setNode(BlockPos.ORIGIN.down().down());
        Network network02 = networkManager.onNodeAdded(nodeAdapter, node02.getPosition());

        assertThat(networkManager.getNetworks()).hasSize(2);
        assertThat(network01).isNotSameAs(network02);
        assertThat(networkManager.getNetworks()).anySatisfy(network -> {
            assertThat(network.getNodeReferences()).containsExactly(new StubNetworkNodeReference(node02));
            assertThat(network02).isSameAs(network);
            assertThat(node02.getNetwork()).isSameAs(network);
        });

        NetworkNode node03 = nodeAdapter.setNode(BlockPos.ORIGIN.down());
        Network network03 = networkManager.onNodeAdded(nodeAdapter, node03.getPosition());

        assertThat(networkManager.getNetworks()).hasSize(1);
        assertThat(networkManager.getNetworks()).anySatisfy(network -> {
            assertThat(network.getNodeReferences()).containsExactlyInAnyOrder(
                    new StubNetworkNodeReference(node01),
                    new StubNetworkNodeReference(node02),
                    new StubNetworkNodeReference(node03)
            );
            assertThat(network03).isSameAs(network);
            assertThat(node01.getNetwork()).isSameAs(network);
            assertThat(node02.getNetwork()).isSameAs(network);
            assertThat(node03.getNetwork()).isSameAs(network);
        });
    }

    @Test
    void Test_splitting_networks_in_two_networks() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        // Act & assert
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        networkManager.onNodeAdded(nodeAdapter, node01.getPosition());

        NetworkNode node02 = nodeAdapter.setNode(BlockPos.ORIGIN.down());
        networkManager.onNodeAdded(nodeAdapter, node02.getPosition());

        NetworkNode node03 = nodeAdapter.setNode(BlockPos.ORIGIN.down().down());
        networkManager.onNodeAdded(nodeAdapter, node03.getPosition());

        assertThat(networkManager.getNetworks()).hasSize(1);

        networkManager.onNodeRemoved(nodeAdapter, node02.getPosition());
        nodeAdapter.removeNode(BlockPos.ORIGIN.down());

        assertThat(networkManager.getNetworks()).hasSize(2);
        assertThat(networkManager.getNetworks()).anySatisfy(network -> {
            assertThat(network.getNodeReferences()).containsExactly(new StubNetworkNodeReference(node01));
            assertThat(node01.getNetwork()).isSameAs(network);
        });
        assertThat(networkManager.getNetworks()).anySatisfy(network -> {
            assertThat(network.getNodeReferences()).containsExactly(new StubNetworkNodeReference(node03));
            assertThat(node03.getNetwork()).isSameAs(network);
        });
    }

    @Test
    void Test_splitting_networks_in_three_networks() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        // Act & assert
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        networkManager.onNodeAdded(nodeAdapter, node01.getPosition());

        NetworkNode node02 = nodeAdapter.setNode(BlockPos.ORIGIN.north());
        networkManager.onNodeAdded(nodeAdapter, node02.getPosition());

        NetworkNode node03 = nodeAdapter.setNode(BlockPos.ORIGIN.east());
        networkManager.onNodeAdded(nodeAdapter, node03.getPosition());

        NetworkNode node04 = nodeAdapter.setNode(BlockPos.ORIGIN.up());
        networkManager.onNodeAdded(nodeAdapter, node04.getPosition());

        assertThat(networkManager.getNetworks()).hasSize(1);

        networkManager.onNodeRemoved(nodeAdapter, node01.getPosition());
        nodeAdapter.removeNode(BlockPos.ORIGIN);

        assertThat(networkManager.getNetworks()).hasSize(3);
        assertThat(networkManager.getNetworks()).anySatisfy(network -> {
            assertThat(network.getNodeReferences()).containsExactly(new StubNetworkNodeReference(node02));
            assertThat(node02.getNetwork()).isSameAs(network);
        });
        assertThat(networkManager.getNetworks()).anySatisfy(network -> {
            assertThat(network.getNodeReferences()).containsExactly(new StubNetworkNodeReference(node03));
            assertThat(node03.getNetwork()).isSameAs(network);
        });
        assertThat(networkManager.getNetworks()).anySatisfy(network -> {
            assertThat(network.getNodeReferences()).containsExactly(new StubNetworkNodeReference(node04));
            assertThat(node04.getNetwork()).isSameAs(network);
        });
    }

    @Test
    void Test_when_removing_a_node_having_a_neighboring_node_without_network_should_fail() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        // Act
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        networkManager.onNodeAdded(nodeAdapter, node01.getPosition());

        NetworkNode node02 = nodeAdapter.setNode(BlockPos.ORIGIN.down());
        networkManager.onNodeAdded(nodeAdapter, node02.getPosition());

        node02.setNetwork(null);

        Executable action = () -> networkManager.onNodeRemoved(nodeAdapter, node01.getPosition());

        // Assert
        NetworkManagerException e = assertThrows(NetworkManagerException.class, action);
        assertThat(e.getMessage()).isEqualTo("The network manager was left in an invalid state. Network node at BlockPos{x=0, y=-1, z=0} has no network!");
    }

    @Test
    void Test_removing_last_node_removes_network() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        // Act
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        networkManager.onNodeAdded(nodeAdapter, node01.getPosition());

        NetworkNode node02 = nodeAdapter.setNode(BlockPos.ORIGIN.down());
        networkManager.onNodeAdded(nodeAdapter, node02.getPosition());
        int sizeBeforeRemoving = networkManager.getNetworks().size();

        networkManager.onNodeRemoved(nodeAdapter, node01.getPosition());
        nodeAdapter.removeNode(BlockPos.ORIGIN);
        int sizeAfterRemovingFirst = networkManager.getNetworks().size();

        networkManager.onNodeRemoved(nodeAdapter, node02.getPosition());
        nodeAdapter.removeNode(BlockPos.ORIGIN.down());
        int sizeAfterRemovingLast = networkManager.getNetworks().size();

        // Assert
        assertThat(sizeBeforeRemoving).isEqualTo(1);
        assertThat(sizeAfterRemovingFirst).isEqualTo(1);
        assertThat(sizeAfterRemovingLast).isZero();
    }

    @Test
    void Test_removing_a_node_with_unmatched_neighbor_network_should_fail() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        // Act
        NetworkNode node01 = nodeAdapter.setNode(BlockPos.ORIGIN);
        networkManager.onNodeAdded(nodeAdapter, node01.getPosition());

        NetworkNode node02 = nodeAdapter.setNode(BlockPos.ORIGIN.down());
        networkManager.onNodeAdded(nodeAdapter, node02.getPosition());

        node02.setNetwork(new NetworkImpl(UUID.randomUUID()));

        Executable action = () -> networkManager.onNodeRemoved(nodeAdapter, node01.getPosition());

        // Assert
        NetworkManagerException e = assertThrows(NetworkManagerException.class, action);
        assertThat(e.getMessage()).isEqualTo("The network manager was left in invalid state. The network of a neighboring node doesn't match the origin node. The origin node is located at BlockPos{x=0, y=0, z=0}");
    }

    @Test
    void Test_removing_a_node_that_does_not_exist_should_fail() {
        // Arrange
        FakeNetworkNodeAdapter nodeAdapter = new FakeNetworkNodeAdapter();

        // Act
        Executable action = () -> networkManager.onNodeRemoved(nodeAdapter, BlockPos.ORIGIN);

        // Assert
        NetworkManagerException e = assertThrows(NetworkManagerException.class, action);
        assertThat(e.getMessage()).isEqualTo("The node at BlockPos{x=0, y=0, z=0} is not present");
    }
}
