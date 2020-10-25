package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.network.node.FakeNetworkNodeAdapter;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.FakeNetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.StubNetworkNodeReference;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@RefinedStorage2Test
class NetworkManagerImplTest {
    @Test
    void Test_adding_node_should_form_network() {
        // Arrange
        FakeNetworkNodeAdapter networkNodeAdapter = new FakeNetworkNodeAdapter();

        NetworkNode node01 = networkNodeAdapter.setNode(BlockPos.ORIGIN, new FakeNetworkNode());

        NetworkManager networkManager = new NetworkManagerImpl(networkNodeAdapter);

        // Act
        Network network01 = networkManager.onNodeAdded(BlockPos.ORIGIN);

        // Assert
        assertThat(network01.getNodeReferences()).containsExactlyInAnyOrder(
                new StubNetworkNodeReference(node01)
        );
        assertThat(node01.getNetwork()).isSameAs(network01);
    }

    @Test
    void Test_adding_node_should_join_existing_network() {
        // Arrange
        FakeNetworkNodeAdapter networkNodeAdapter = new FakeNetworkNodeAdapter();

        NetworkManager networkManager = new NetworkManagerImpl(networkNodeAdapter);

        // Act & assert
        NetworkNode node01 = networkNodeAdapter.setNode(BlockPos.ORIGIN, new FakeNetworkNode());
        Network network01 = networkManager.onNodeAdded(BlockPos.ORIGIN);

        assertThat(network01.getNodeReferences()).containsExactlyInAnyOrder(
                new StubNetworkNodeReference(node01)
        );
        assertThat(node01.getNetwork()).isSameAs(network01);

        NetworkNode node02 = networkNodeAdapter.setNode(BlockPos.ORIGIN.down(), new FakeNetworkNode());
        Network network02 = networkManager.onNodeAdded(BlockPos.ORIGIN.down());

        assertThat(network02).isSameAs(network01);
        assertThat(node02.getNetwork()).isSameAs(network02);
        assertThat(network02.getNodeReferences()).containsExactlyInAnyOrder(
                new StubNetworkNodeReference(node01),
                new StubNetworkNodeReference(node02)
        );
    }
}
