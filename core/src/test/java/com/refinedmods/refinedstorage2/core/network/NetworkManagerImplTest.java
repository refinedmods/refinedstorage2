package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.adapter.FakeWorldAdapter;
import com.refinedmods.refinedstorage2.core.network.node.FakeNetworkNodeAdapter;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.StubNetworkNodeReference;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RefinedStorage2Test
class NetworkManagerImplTest {
    @Test
    void Test_adding_node_should_form_network() {
        // Arrange
        FakeNetworkNodeAdapter networkNodeAdapter = new FakeNetworkNodeAdapter();
        FakeWorldAdapter fakeWorldAdapter = new FakeWorldAdapter();

        NetworkNode n01 = networkNodeAdapter.setNode(BlockPos.ORIGIN, mock(NetworkNode.class));

        NetworkManager networkManager = new NetworkManagerImpl(networkNodeAdapter);

        // Act
        Network network = networkManager.onNodeAdded(fakeWorldAdapter, BlockPos.ORIGIN);

        // Assert
        assertThat(network.getNodeReferences()).containsExactlyInAnyOrder(
                new StubNetworkNodeReference(BlockPos.ORIGIN, n01)
        );
    }

    @Test
    void Test_adding_node_should_join_existing_network() {
        // Arrange
        FakeNetworkNodeAdapter networkNodeAdapter = new FakeNetworkNodeAdapter();
        FakeWorldAdapter fakeWorldAdapter = new FakeWorldAdapter();

        NetworkNode n01 = networkNodeAdapter.setNode(BlockPos.ORIGIN, mock(NetworkNode.class));
        NetworkNode n02 = networkNodeAdapter.setNode(BlockPos.ORIGIN.down(), mock(NetworkNode.class));

        NetworkManager networkManager = new NetworkManagerImpl(networkNodeAdapter);

        // Act & assert
        Network network01 = networkManager.onNodeAdded(fakeWorldAdapter, BlockPos.ORIGIN);

        assertThat(network01.getNodeReferences()).containsExactlyInAnyOrder(
                new StubNetworkNodeReference(BlockPos.ORIGIN, n01)
        );

        Network network02 = networkManager.onNodeAdded(fakeWorldAdapter, BlockPos.ORIGIN.down());

        assertThat(network02).isSameAs(network01);
        assertThat(network02.getNodeReferences()).containsExactlyInAnyOrder(
                new StubNetworkNodeReference(BlockPos.ORIGIN, n01),
                new StubNetworkNodeReference(BlockPos.ORIGIN.down(), n02)
        );
    }
}
