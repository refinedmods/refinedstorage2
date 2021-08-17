package com.refinedmods.refinedstorage2.core.network.node.container;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.adapter.FakeRs2World;
import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.NetworkUtil;
import com.refinedmods.refinedstorage2.core.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class NetworkNodeContainerImplTest {
    @Test
    void Test_forming_new_network() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeContainer<?> container = NetworkUtil.createContainer(world, Position.ORIGIN);
        NetworkNodeContainer<?> unrelatedContainer = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.down().down());

        FakeNetworkNodeContainerRepository repository = FakeNetworkNodeContainerRepository.of(container, unrelatedContainer);

        // Act
        container.initialize(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        assertThat(container.getNode().getNetwork()).isNotNull();
        assertThat(container.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactly(NetworkNodeContainerEntry.create(container));

        assertThat(NetworkUtil.getAddedContainers(container.getNode().getNetwork())).containsExactly(container);
        assertThat(NetworkUtil.getRemovedContainers(container.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(container.getNode().getNetwork())).isZero();
        assertThat(NetworkUtil.getNetworkMerges(container.getNode().getNetwork())).isEmpty();

        assertThat(unrelatedContainer.getNode().getNetwork()).isNotNull().isNotEqualTo(container.getNode().getNetwork());
        assertThat(unrelatedContainer.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactly(NetworkNodeContainerEntry.create(unrelatedContainer));
    }

    @Test
    void Test_joining_existing_network() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeContainer<?> existingContianer1 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN);
        NetworkNodeContainer<?> existingContainer2 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.up(), container -> existingContianer1.getNode().getNetwork());
        NetworkNodeContainer<?> newContainer = NetworkUtil.createContainer(world, Position.ORIGIN.down());
        NetworkNodeContainer<?> unrelatedContainer = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.down().down().down());

        FakeNetworkNodeContainerRepository repository = FakeNetworkNodeContainerRepository.of(existingContianer1, existingContainer2, newContainer, unrelatedContainer);

        // Act
        newContainer.initialize(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        Network expectedNetwork = existingContianer1.getNode().getNetwork();

        assertThat(existingContianer1.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(existingContainer2.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(newContainer.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(expectedNetwork.getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                NetworkNodeContainerEntry.create(existingContianer1),
                NetworkNodeContainerEntry.create(existingContainer2),
                NetworkNodeContainerEntry.create(newContainer)
        );

        assertThat(NetworkUtil.getAddedContainers(expectedNetwork)).containsExactly(existingContianer1, existingContainer2, newContainer);
        assertThat(NetworkUtil.getRemovedContainers(expectedNetwork)).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(expectedNetwork)).isZero();
        assertThat(NetworkUtil.getNetworkMerges(expectedNetwork)).isEmpty();

        assertThat(unrelatedContainer.getNode().getNetwork()).isNotNull().isNotEqualTo(expectedNetwork);
        assertThat(unrelatedContainer.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactly(NetworkNodeContainerEntry.create(unrelatedContainer));
    }

    @Test
    void Test_merging_with_existing_networks() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeContainer<?> existingContainer0 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.up());
        NetworkNodeContainer<?> existingContainer1 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN, container -> existingContainer0.getNode().getNetwork());
        NetworkNodeContainer<?> existingContainer2 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.down().down());
        Network initialNetworkOfExistingContainer1 = existingContainer1.getNode().getNetwork();
        NetworkNodeContainer<?> unrelatedContainer = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.east().east());
        NetworkNodeContainer<?> newContainer = NetworkUtil.createContainer(world, Position.ORIGIN.down());

        FakeNetworkNodeContainerRepository repository = FakeNetworkNodeContainerRepository.of(existingContainer1, existingContainer2, existingContainer0, newContainer, unrelatedContainer);

        // Act
        newContainer.initialize(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        Network expectedNetwork = existingContainer2.getNode().getNetwork();

        assertThat(NetworkUtil.getNetworkMerges(initialNetworkOfExistingContainer1)).containsExactlyInAnyOrder(expectedNetwork);

        assertThat(existingContainer1.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(existingContainer2.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(existingContainer0.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(newContainer.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(expectedNetwork.getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                NetworkNodeContainerEntry.create(existingContainer1),
                NetworkNodeContainerEntry.create(existingContainer2),
                NetworkNodeContainerEntry.create(existingContainer0),
                NetworkNodeContainerEntry.create(newContainer)
        );

        assertThat(NetworkUtil.getAddedContainers(expectedNetwork)).containsExactlyInAnyOrder(existingContainer2, newContainer, existingContainer1, existingContainer0);
        assertThat(NetworkUtil.getRemovedContainers(expectedNetwork)).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(expectedNetwork)).isZero();
        assertThat(NetworkUtil.getNetworkMerges(expectedNetwork)).isEmpty();

        assertThat(unrelatedContainer.getNode().getNetwork()).isNotNull().isNotEqualTo(expectedNetwork);
        assertThat(unrelatedContainer.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactly(NetworkNodeContainerEntry.create(unrelatedContainer));
    }

    @Test
    void Test_should_form_network_if_there_are_neighbors_with_no_network() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeContainer<?> unrelatedContainer = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.east().east());

        NetworkNodeContainer<?> container1 = NetworkUtil.createContainer(world, Position.ORIGIN);
        NetworkNodeContainer<?> container2 = NetworkUtil.createContainer(world, Position.ORIGIN.down());
        NetworkNodeContainer<?> container3 = NetworkUtil.createContainer(world, Position.ORIGIN.down().down());

        FakeNetworkNodeContainerRepository repository = FakeNetworkNodeContainerRepository.of(container1, container2, container3, unrelatedContainer);

        // Act
        container1.initialize(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);
        container2.initialize(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);
        container3.initialize(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        Network expectedNetwork = container1.getNode().getNetwork();

        assertThat(container1.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(container2.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(container3.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(expectedNetwork.getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                NetworkNodeContainerEntry.create(container1),
                NetworkNodeContainerEntry.create(container2),
                NetworkNodeContainerEntry.create(container3)
        );

        assertThat(NetworkUtil.getNetworkMerges(expectedNetwork)).isEmpty();
        assertThat(NetworkUtil.getAddedContainers(expectedNetwork)).containsExactlyInAnyOrder(container1, container2, container3);
        assertThat(NetworkUtil.getRemovedContainers(expectedNetwork)).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(expectedNetwork)).isZero();

        assertThat(unrelatedContainer.getNode().getNetwork()).isNotNull().isNotEqualTo(expectedNetwork);
        assertThat(unrelatedContainer.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactly(NetworkNodeContainerEntry.create(unrelatedContainer));
    }

    @Test
    void Test_should_split_network() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeContainer<?> container1 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN);
        NetworkNodeContainer<?> container2 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.down(), container -> container1.getNode().getNetwork());
        NetworkNodeContainer<?> container3 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.down().down(), container -> container1.getNode().getNetwork());

        NetworkNodeContainer<?> unrelatedContainer = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.east().east());

        FakeNetworkNodeContainerRepository repository = FakeNetworkNodeContainerRepository.of(container1, container2, container3, unrelatedContainer);

        // Act
        repository.removeContainer(container1.getPosition());
        container1.remove(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        assertThat(container1.getNode().getNetwork()).isNull();

        assertThat(container2.getNode().getNetwork())
                .isSameAs(container3.getNode().getNetwork())
                .isNotSameAs(unrelatedContainer.getNode().getNetwork());

        assertThat(container2.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                NetworkNodeContainerEntry.create(container2),
                NetworkNodeContainerEntry.create(container3)
        );

        assertThat(NetworkUtil.getNetworkSplits(container2.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getAddedContainers(container2.getNode().getNetwork())).containsExactly(container1, container2, container3);
        assertThat(NetworkUtil.getRemovedContainers(container2.getNode().getNetwork())).containsExactly(container1);
        assertThat(NetworkUtil.getNetworkRemovedCount(container2.getNode().getNetwork())).isZero();
    }

    @Test
    void Test_should_split_network_in_two() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeContainer<?> container1 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN);
        NetworkNodeContainer<?> container2 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.down(), container -> container1.getNode().getNetwork());

        NetworkNodeContainer<?> container3 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.down().down(), container -> container1.getNode().getNetwork());

        NetworkNodeContainer<?> container4 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.down().down().down(), container -> container1.getNode().getNetwork());
        NetworkNodeContainer<?> container5 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.down().down().down().down(), container -> container1.getNode().getNetwork());

        NetworkNodeContainer<?> unrelatedContainer = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.east().east());

        FakeNetworkNodeContainerRepository repository = FakeNetworkNodeContainerRepository.of(container1, container2, container3, container4, container5, unrelatedContainer);

        // Act
        repository.removeContainer(container3.getPosition());
        container3.remove(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        assertThat(container3.getNode().getNetwork()).isNull();

        assertThat(container1.getNode().getNetwork())
                .isSameAs(container2.getNode().getNetwork())
                .isNotSameAs(unrelatedContainer.getNode().getNetwork())
                .isNotSameAs(container4.getNode().getNetwork())
                .isNotSameAs(container5.getNode().getNetwork());

        assertThat(NetworkUtil.getNetworkSplits(container1.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getAddedContainers(container1.getNode().getNetwork())).containsExactlyInAnyOrder(container1, container2);
        assertThat(NetworkUtil.getRemovedContainers(container1.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(container1.getNode().getNetwork())).isZero();

        assertThat(container1.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                NetworkNodeContainerEntry.create(container1),
                NetworkNodeContainerEntry.create(container2)
        );

        assertThat(container4.getNode().getNetwork())
                .isSameAs(container5.getNode().getNetwork())
                .isNotSameAs(unrelatedContainer.getNode().getNetwork())
                .isNotSameAs(container1.getNode().getNetwork())
                .isNotSameAs(container2.getNode().getNetwork());

        List<Set<Network>> splits = NetworkUtil.getNetworkSplits(container4.getNode().getNetwork());
        assertThat(splits).hasSize(1);
        assertThat(splits.get(0)).containsExactlyInAnyOrder(container1.getNode().getNetwork());

        assertThat(NetworkUtil.getAddedContainers(container4.getNode().getNetwork())).containsExactlyInAnyOrder(container1, container2, container3, container4, container5);
        assertThat(NetworkUtil.getRemovedContainers(container4.getNode().getNetwork())).containsExactlyInAnyOrder(container1, container2, container3);
        assertThat(NetworkUtil.getNetworkRemovedCount(container4.getNode().getNetwork())).isZero();

        assertThat(container4.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                NetworkNodeContainerEntry.create(container4),
                NetworkNodeContainerEntry.create(container5)
        );
    }

    @Test
    void Test_should_split_network_in_three() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeContainer<?> container1 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN);

        NetworkNodeContainer<?> container2 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.up(), container -> container1.getNode().getNetwork());

        NetworkNodeContainer<?> container3 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.down(), container -> container1.getNode().getNetwork());

        NetworkNodeContainer<?> container4 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.north(), container -> container1.getNode().getNetwork());
        NetworkNodeContainer<?> container5 = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.north().north(), container -> container1.getNode().getNetwork());

        NetworkNodeContainer<?> unrelatedContainer = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.south().south());

        FakeNetworkNodeContainerRepository repository = FakeNetworkNodeContainerRepository.of(container1, container2, container3, container4, container5, unrelatedContainer);

        // Act
        repository.removeContainer(container1.getPosition());
        container1.remove(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        assertThat(container1.getNode().getNetwork()).isNull();

        assertThat(container2.getNode().getNetwork())
                .isNotSameAs(container3.getNode().getNetwork())
                .isNotSameAs(container4.getNode().getNetwork())
                .isNotSameAs(container5.getNode().getNetwork())
                .isNotSameAs(unrelatedContainer.getNode().getNetwork());

        assertThat(NetworkUtil.getNetworkSplits(container2.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getAddedContainers(container2.getNode().getNetwork())).containsExactlyInAnyOrder(container2);
        assertThat(NetworkUtil.getRemovedContainers(container2.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(container2.getNode().getNetwork())).isZero();

        assertThat(container3.getNode().getNetwork())
                .isNotSameAs(container2.getNode().getNetwork())
                .isNotSameAs(container4.getNode().getNetwork())
                .isNotSameAs(container5.getNode().getNetwork())
                .isNotSameAs(unrelatedContainer.getNode().getNetwork());

        List<Set<Network>> splits = NetworkUtil.getNetworkSplits(container3.getNode().getNetwork());
        assertThat(splits).hasSize(1);
        assertThat(splits.get(0)).containsExactlyInAnyOrder(container2.getNode().getNetwork(), container4.getNode().getNetwork());

        assertThat(NetworkUtil.getAddedContainers(container3.getNode().getNetwork())).containsExactlyInAnyOrder(container1, container2, container3, container4, container5);
        assertThat(NetworkUtil.getRemovedContainers(container3.getNode().getNetwork())).containsExactlyInAnyOrder(container1, container2, container4, container5);
        assertThat(NetworkUtil.getNetworkRemovedCount(container3.getNode().getNetwork())).isZero();

        assertThat(container4.getNode().getNetwork())
                .isNotSameAs(container2.getNode().getNetwork())
                .isNotSameAs(container3.getNode().getNetwork())
                .isSameAs(container5.getNode().getNetwork())
                .isNotSameAs(unrelatedContainer.getNode().getNetwork());

        assertThat(NetworkUtil.getNetworkSplits(container4.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getAddedContainers(container4.getNode().getNetwork())).containsExactlyInAnyOrder(container4, container5);
        assertThat(NetworkUtil.getRemovedContainers(container4.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(container4.getNode().getNetwork())).isZero();

        assertThat(container2.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                NetworkNodeContainerEntry.create(container2)
        );

        assertThat(container3.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                NetworkNodeContainerEntry.create(container3)
        );

        assertThat(container4.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                NetworkNodeContainerEntry.create(container4),
                NetworkNodeContainerEntry.create(container5)
        );
    }

    @Test
    void Test_should_remove_network() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeContainer<?> container = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN);
        NetworkNodeContainer<?> unrelatedContainer = NetworkUtil.createContainerWithNetwork(world, Position.ORIGIN.south().south());

        FakeNetworkNodeContainerRepository repository = FakeNetworkNodeContainerRepository.of(container, unrelatedContainer);

        Network network = container.getNode().getNetwork();

        // Act
        repository.removeContainer(container.getPosition());
        container.remove(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        assertThat(container.getNode().getNetwork()).isNull();

        assertThat(unrelatedContainer.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                NetworkNodeContainerEntry.create(unrelatedContainer)
        );

        assertThat(NetworkUtil.getNetworkSplits(network)).isEmpty();
        assertThat(NetworkUtil.getAddedContainers(network)).containsExactly(container);
        assertThat(NetworkUtil.getRemovedContainers(network)).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(network)).isEqualTo(1);
    }
}
