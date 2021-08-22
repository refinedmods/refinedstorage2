package com.refinedmods.refinedstorage2.api.network.node.container;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.FakeConnectionProvider;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.NETWORK_COMPONENT_REGISTRY;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.createContainer;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.createContainerWithNetwork;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.getAddedContainers;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.getNetworkMerges;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.getNetworkRemovedCount;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.getNetworkSplits;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.getRemovedContainers;
import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class NetworkNodeContainerImplTest {
    @Test
    void Test_forming_new_network() {
        // Arrange
        FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        NetworkNodeContainer<?> container = createContainer();
        NetworkNodeContainer<?> unrelatedContainer = createContainerWithNetwork();

        connectionProvider.with(container, unrelatedContainer);

        // Act
        container.initialize(connectionProvider, NETWORK_COMPONENT_REGISTRY);

        // Assert
        assertThat(container.getNode().getNetwork()).isNotNull();
        assertThat(container.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactly(container);

        assertThat(getAddedContainers(container.getNode().getNetwork())).containsExactly(container);
        assertThat(getRemovedContainers(container.getNode().getNetwork())).isEmpty();
        assertThat(getNetworkRemovedCount(container.getNode().getNetwork())).isZero();
        assertThat(getNetworkMerges(container.getNode().getNetwork())).isEmpty();

        assertThat(unrelatedContainer.getNode().getNetwork()).isNotNull().isNotEqualTo(container.getNode().getNetwork());
        assertThat(unrelatedContainer.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactly(unrelatedContainer);
    }

    @Test
    void Test_joining_existing_network() {
        // Arrange
        FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        NetworkNodeContainer<?> existingContainer1 = createContainerWithNetwork();
        NetworkNodeContainer<?> existingContainer2 = createContainerWithNetwork(container -> existingContainer1.getNode().getNetwork());
        NetworkNodeContainer<?> newContainer = createContainer();
        NetworkNodeContainer<?> unrelatedContainer = createContainerWithNetwork();

        connectionProvider
                .with(existingContainer1)
                .with(existingContainer2)
                .with(newContainer)
                .with(unrelatedContainer)
                .connect(existingContainer1, existingContainer2)
                .connect(existingContainer1, newContainer);

        // Act
        newContainer.initialize(connectionProvider, NETWORK_COMPONENT_REGISTRY);

        // Assert
        Network expectedNetwork = existingContainer1.getNode().getNetwork();

        assertThat(existingContainer1.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(existingContainer2.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(newContainer.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(expectedNetwork.getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                existingContainer1,
                existingContainer2,
                newContainer
        );

        assertThat(getAddedContainers(expectedNetwork)).containsExactly(existingContainer1, existingContainer2, newContainer);
        assertThat(getRemovedContainers(expectedNetwork)).isEmpty();
        assertThat(getNetworkRemovedCount(expectedNetwork)).isZero();
        assertThat(getNetworkMerges(expectedNetwork)).isEmpty();

        assertThat(unrelatedContainer.getNode().getNetwork()).isNotNull().isNotEqualTo(expectedNetwork);
        assertThat(unrelatedContainer.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactly(unrelatedContainer);
    }

    @Test
    void Test_merging_with_existing_networks() {
        // Arrange
        FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        NetworkNodeContainer<?> existingContainer0 = createContainerWithNetwork();
        NetworkNodeContainer<?> existingContainer1 = createContainerWithNetwork(container -> existingContainer0.getNode().getNetwork());
        NetworkNodeContainer<?> existingContainer2 = createContainerWithNetwork();
        Network initialNetworkOfExistingContainer2 = existingContainer2.getNode().getNetwork();
        NetworkNodeContainer<?> unrelatedContainer = createContainerWithNetwork();
        NetworkNodeContainer<?> newContainer = createContainer();

        connectionProvider
                .with(existingContainer0, existingContainer1, existingContainer2, unrelatedContainer, newContainer)
                .connect(existingContainer0, existingContainer1)
                .connect(existingContainer2, newContainer)
                .connect(newContainer, existingContainer1);

        // Act
        newContainer.initialize(connectionProvider, NETWORK_COMPONENT_REGISTRY);

        // Assert
        Network expectedNetwork = existingContainer1.getNode().getNetwork();

        assertThat(getNetworkMerges(initialNetworkOfExistingContainer2)).containsExactlyInAnyOrder(expectedNetwork);

        assertThat(existingContainer1.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(existingContainer2.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(existingContainer0.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(newContainer.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(expectedNetwork.getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                existingContainer1,
                existingContainer2,
                existingContainer0,
                newContainer
        );

        assertThat(getAddedContainers(expectedNetwork)).containsExactlyInAnyOrder(existingContainer2, newContainer, existingContainer1, existingContainer0);
        assertThat(getRemovedContainers(expectedNetwork)).isEmpty();
        assertThat(getNetworkRemovedCount(expectedNetwork)).isZero();
        assertThat(getNetworkMerges(expectedNetwork)).isEmpty();

        assertThat(unrelatedContainer.getNode().getNetwork()).isNotNull().isNotEqualTo(expectedNetwork);
        assertThat(unrelatedContainer.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactly(unrelatedContainer);
    }

    @Test
    void Test_should_form_network_if_there_are_neighbors_with_no_network() {
        // Arrange
        FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        NetworkNodeContainer<?> unrelatedContainer = createContainerWithNetwork();

        NetworkNodeContainer<?> container1 = createContainer();
        NetworkNodeContainer<?> container2 = createContainer();
        NetworkNodeContainer<?> container3 = createContainer();

        connectionProvider.with(container1, container2, container3)
                .connect(container1, container2)
                .connect(container2, container3);

        // Act
        container1.initialize(connectionProvider, NETWORK_COMPONENT_REGISTRY);
        container2.initialize(connectionProvider, NETWORK_COMPONENT_REGISTRY);
        container3.initialize(connectionProvider, NETWORK_COMPONENT_REGISTRY);

        // Assert
        Network expectedNetwork = container1.getNode().getNetwork();

        assertThat(container1.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(container2.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(container3.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(expectedNetwork.getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                container1,
                container2,
                container3
        );

        assertThat(getNetworkMerges(expectedNetwork)).isEmpty();
        assertThat(getAddedContainers(expectedNetwork)).containsExactlyInAnyOrder(container1, container2, container3);
        assertThat(getRemovedContainers(expectedNetwork)).isEmpty();
        assertThat(getNetworkRemovedCount(expectedNetwork)).isZero();

        assertThat(unrelatedContainer.getNode().getNetwork()).isNotNull().isNotEqualTo(expectedNetwork);
        assertThat(unrelatedContainer.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactly(unrelatedContainer);
    }

    @Test
    void Test_should_split_network() {
        // Arrange
        FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        NetworkNodeContainer<?> container1 = createContainerWithNetwork();
        NetworkNodeContainer<?> container2 = createContainerWithNetwork(container -> container1.getNode().getNetwork());
        NetworkNodeContainer<?> container3 = createContainerWithNetwork(container -> container1.getNode().getNetwork());

        NetworkNodeContainer<?> unrelatedContainer = createContainerWithNetwork();

        connectionProvider
                .with(container1, container2, container3, unrelatedContainer)
                .connect(container2, container3);

        // Act
        container1.remove(connectionProvider, NETWORK_COMPONENT_REGISTRY);

        // Assert
        assertThat(container1.getNode().getNetwork()).isNull();

        assertThat(container2.getNode().getNetwork())
                .isSameAs(container3.getNode().getNetwork())
                .isNotSameAs(unrelatedContainer.getNode().getNetwork());

        assertThat(container2.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                container2,
                container3
        );

        assertThat(getNetworkSplits(container2.getNode().getNetwork())).isEmpty();
        assertThat(getAddedContainers(container2.getNode().getNetwork())).containsExactly(container1, container2, container3);
        assertThat(getRemovedContainers(container2.getNode().getNetwork())).containsExactly(container1);
        assertThat(getNetworkRemovedCount(container2.getNode().getNetwork())).isZero();
    }

    @Test
    void Test_should_split_network_in_two() {
        // Arrange
        FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        NetworkNodeContainer<?> container1 = createContainerWithNetwork();
        NetworkNodeContainer<?> container2 = createContainerWithNetwork(container -> container1.getNode().getNetwork());

        NetworkNodeContainer<?> container3 = createContainerWithNetwork(container -> container1.getNode().getNetwork());

        NetworkNodeContainer<?> container4 = createContainerWithNetwork(container -> container1.getNode().getNetwork());
        NetworkNodeContainer<?> container5 = createContainerWithNetwork(container -> container1.getNode().getNetwork());

        NetworkNodeContainer<?> unrelatedContainer = createContainerWithNetwork();

        connectionProvider
                .with(container5, container4, container3, container2, container1, unrelatedContainer)
                .connect(container1, container2)
                .connect(container4, container5);

        // Act
        container3.remove(connectionProvider, NETWORK_COMPONENT_REGISTRY);

        // Assert
        assertThat(container3.getNode().getNetwork()).isNull();

        assertThat(container1.getNode().getNetwork())
                .isSameAs(container2.getNode().getNetwork())
                .isNotSameAs(unrelatedContainer.getNode().getNetwork())
                .isNotSameAs(container4.getNode().getNetwork())
                .isNotSameAs(container5.getNode().getNetwork());

        assertThat(getNetworkSplits(container1.getNode().getNetwork())).isEmpty();
        assertThat(getAddedContainers(container1.getNode().getNetwork())).containsExactlyInAnyOrder(container1, container2);
        assertThat(getRemovedContainers(container1.getNode().getNetwork())).isEmpty();
        assertThat(getNetworkRemovedCount(container1.getNode().getNetwork())).isZero();

        assertThat(container1.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                container1,
                container2
        );

        assertThat(container4.getNode().getNetwork())
                .isSameAs(container5.getNode().getNetwork())
                .isNotSameAs(unrelatedContainer.getNode().getNetwork())
                .isNotSameAs(container1.getNode().getNetwork())
                .isNotSameAs(container2.getNode().getNetwork());

        List<Set<Network>> splits = getNetworkSplits(container4.getNode().getNetwork());
        assertThat(splits).hasSize(1);
        assertThat(splits.get(0)).containsExactlyInAnyOrder(container1.getNode().getNetwork());

        assertThat(getAddedContainers(container4.getNode().getNetwork())).containsExactlyInAnyOrder(container1, container2, container3, container4, container5);
        assertThat(getRemovedContainers(container4.getNode().getNetwork())).containsExactlyInAnyOrder(container1, container2, container3);
        assertThat(getNetworkRemovedCount(container4.getNode().getNetwork())).isZero();

        assertThat(container4.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                container4,
                container5
        );
    }

    @Test
    void Test_should_split_network_in_three() {
        // Arrange
        FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        NetworkNodeContainer<?> container1 = createContainerWithNetwork();

        NetworkNodeContainer<?> container2 = createContainerWithNetwork(container -> container1.getNode().getNetwork());

        NetworkNodeContainer<?> container3 = createContainerWithNetwork(container -> container1.getNode().getNetwork());

        NetworkNodeContainer<?> container4 = createContainerWithNetwork(container -> container1.getNode().getNetwork());
        NetworkNodeContainer<?> container5 = createContainerWithNetwork(container -> container1.getNode().getNetwork());

        NetworkNodeContainer<?> unrelatedContainer = createContainerWithNetwork();

        connectionProvider
                .with(unrelatedContainer, container5, container4, container3, container2, container1)
                .connect(container4, container5);

        // Act
        container1.remove(connectionProvider, NETWORK_COMPONENT_REGISTRY);

        // Assert
        assertThat(container1.getNode().getNetwork()).isNull();

        assertThat(container2.getNode().getNetwork())
                .isNotSameAs(container3.getNode().getNetwork())
                .isNotSameAs(container4.getNode().getNetwork())
                .isNotSameAs(container5.getNode().getNetwork())
                .isNotSameAs(unrelatedContainer.getNode().getNetwork());

        assertThat(getNetworkSplits(container2.getNode().getNetwork())).isEmpty();
        assertThat(getAddedContainers(container2.getNode().getNetwork())).containsExactlyInAnyOrder(container2);
        assertThat(getRemovedContainers(container2.getNode().getNetwork())).isEmpty();
        assertThat(getNetworkRemovedCount(container2.getNode().getNetwork())).isZero();

        assertThat(container3.getNode().getNetwork())
                .isNotSameAs(container2.getNode().getNetwork())
                .isNotSameAs(container4.getNode().getNetwork())
                .isNotSameAs(container5.getNode().getNetwork())
                .isNotSameAs(unrelatedContainer.getNode().getNetwork());

        assertThat(getNetworkSplits(container3.getNode().getNetwork())).isEmpty();
        assertThat(getAddedContainers(container3.getNode().getNetwork())).containsExactlyInAnyOrder(container3);
        assertThat(getRemovedContainers(container3.getNode().getNetwork())).isEmpty();
        assertThat(getNetworkRemovedCount(container3.getNode().getNetwork())).isZero();

        assertThat(container4.getNode().getNetwork())
                .isNotSameAs(container2.getNode().getNetwork())
                .isNotSameAs(container3.getNode().getNetwork())
                .isSameAs(container5.getNode().getNetwork())
                .isNotSameAs(unrelatedContainer.getNode().getNetwork());

        List<Set<Network>> splits = getNetworkSplits(container4.getNode().getNetwork());
        assertThat(splits).hasSize(1);
        assertThat(splits.get(0)).containsExactlyInAnyOrder(container2.getNode().getNetwork(), container3.getNode().getNetwork());

        assertThat(getAddedContainers(container4.getNode().getNetwork())).containsExactlyInAnyOrder(container1, container2, container3, container4, container5);
        assertThat(getRemovedContainers(container4.getNode().getNetwork())).containsExactlyInAnyOrder(container1, container2, container3);
        assertThat(getNetworkRemovedCount(container4.getNode().getNetwork())).isZero();

        assertThat(container2.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                container2
        );

        assertThat(container3.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                container3
        );

        assertThat(container4.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                container4,
                container5
        );
    }

    @Test
    void Test_should_remove_network() {
        // Arrange
        FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        NetworkNodeContainer<?> container = createContainerWithNetwork();
        NetworkNodeContainer<?> unrelatedContainer = createContainerWithNetwork();

        connectionProvider.with(container, unrelatedContainer);

        Network network = container.getNode().getNetwork();

        // Act
        container.remove(connectionProvider, NETWORK_COMPONENT_REGISTRY);

        // Assert
        assertThat(container.getNode().getNetwork()).isNull();

        assertThat(unrelatedContainer.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
                unrelatedContainer
        );

        assertThat(getNetworkSplits(network)).isEmpty();
        assertThat(getAddedContainers(network)).containsExactly(container);
        assertThat(getRemovedContainers(network)).isEmpty();
        assertThat(getNetworkRemovedCount(network)).isEqualTo(1);
    }
}
