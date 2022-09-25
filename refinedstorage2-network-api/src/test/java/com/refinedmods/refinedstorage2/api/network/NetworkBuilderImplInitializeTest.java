package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NetworkBuilderImplInitializeTest extends AbstractNetworkBuilderImplTest {
    @Test
    void shouldNotFormNetworkIfAlreadyFormed() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();
        final NetworkNodeContainer container = createContainerWithNetwork();

        // Act
        final boolean success = sut.initialize(container, connectionProvider);

        // Assert
        assertThat(success).isFalse();
    }

    @Test
    void shouldFormNetwork() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        final NetworkNodeContainer container = createContainer();
        final NetworkNodeContainer unrelatedContainer = createContainerWithNetwork();

        connectionProvider.with(container, unrelatedContainer);

        // Act
        final boolean success = sut.initialize(container, connectionProvider);

        // Assert
        assertThat(success).isTrue();

        assertThat(container.getNode().getNetwork()).isNotNull();
        assertThat(container.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactly(container);

        assertThat(getAddedContainers(container.getNode().getNetwork())).containsExactly(container);
        assertThat(getRemovedContainers(container.getNode().getNetwork())).isEmpty();
        assertThat(getAmountRemoved(container.getNode().getNetwork())).isZero();
        assertThat(getNetworkMerges(container.getNode().getNetwork())).isEmpty();

        assertThat(unrelatedContainer.getNode().getNetwork())
            .isNotNull()
            .isNotEqualTo(container.getNode().getNetwork());
        assertThat(unrelatedContainer.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactly(unrelatedContainer);
    }

    @Test
    void shouldJoinExistingNetwork() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        final NetworkNodeContainer existingContainer1 = createContainerWithNetwork();
        final NetworkNodeContainer existingContainer2 =
            createContainerWithNetwork(container -> existingContainer1.getNode().getNetwork());
        final NetworkNodeContainer newContainer = createContainer();
        final NetworkNodeContainer unrelatedContainer = createContainerWithNetwork();

        connectionProvider
            .with(existingContainer1)
            .with(existingContainer2)
            .with(newContainer)
            .with(unrelatedContainer)
            .connect(existingContainer1, existingContainer2)
            .connect(existingContainer1, newContainer);

        // Act
        final boolean success = sut.initialize(newContainer, connectionProvider);

        // Assert
        assertThat(success).isTrue();

        final Network expectedNetwork = existingContainer1.getNode().getNetwork();
        assertThat(expectedNetwork).isNotNull();

        assertThat(existingContainer1.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(existingContainer2.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(newContainer.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(expectedNetwork.getComponent(GraphNetworkComponent.class).getContainers()).containsExactlyInAnyOrder(
            existingContainer1,
            existingContainer2,
            newContainer
        );

        assertThat(getAddedContainers(expectedNetwork)).containsExactly(
            existingContainer1,
            existingContainer2,
            newContainer
        );
        assertThat(getRemovedContainers(expectedNetwork)).isEmpty();
        assertThat(getAmountRemoved(expectedNetwork)).isZero();
        assertThat(getNetworkMerges(expectedNetwork)).isEmpty();

        assertThat(unrelatedContainer.getNode().getNetwork()).isNotNull().isNotEqualTo(expectedNetwork);
        assertThat(unrelatedContainer.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactly(unrelatedContainer);
    }

    @Test
    void shouldMergeWithExistingNetworks() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        final NetworkNodeContainer existingContainer0 = createContainerWithNetwork();
        final NetworkNodeContainer existingContainer1 =
            createContainerWithNetwork(container -> existingContainer0.getNode().getNetwork());
        final NetworkNodeContainer existingContainer2 = createContainerWithNetwork();
        final Network initialNetworkOfExistingContainer2 = existingContainer2.getNode().getNetwork();
        final NetworkNodeContainer unrelatedContainer = createContainerWithNetwork();
        final NetworkNodeContainer newContainer = createContainer();

        connectionProvider
            .with(existingContainer0, existingContainer1, existingContainer2, unrelatedContainer, newContainer)
            .connect(existingContainer0, existingContainer1)
            .connect(existingContainer2, newContainer)
            .connect(newContainer, existingContainer1);

        // Act
        final boolean success = sut.initialize(newContainer, connectionProvider);

        // Assert
        assertThat(success).isTrue();

        final Network expectedNetwork = existingContainer1.getNode().getNetwork();
        assertThat(expectedNetwork).isNotNull();

        assertThat(initialNetworkOfExistingContainer2).isNotNull();
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

        assertThat(getAddedContainers(expectedNetwork))
            .containsExactlyInAnyOrder(existingContainer2, newContainer, existingContainer1, existingContainer0);
        assertThat(getRemovedContainers(expectedNetwork)).isEmpty();
        assertThat(getAmountRemoved(expectedNetwork)).isZero();
        assertThat(getNetworkMerges(expectedNetwork)).isEmpty();

        assertThat(unrelatedContainer.getNode().getNetwork()).isNotNull().isNotEqualTo(expectedNetwork);
        assertThat(unrelatedContainer.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactly(unrelatedContainer);
    }

    @Test
    void shouldFormNetworkIfThereAreNeighborsWithoutNetwork() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        final NetworkNodeContainer unrelatedContainer = createContainerWithNetwork();

        final NetworkNodeContainer container1 = createContainer();
        final NetworkNodeContainer container2 = createContainer();
        final NetworkNodeContainer container3 = createContainer();

        connectionProvider.with(container1, container2, container3)
            .connect(container1, container2)
            .connect(container2, container3);

        // Act
        final boolean success1 = sut.initialize(container1, connectionProvider);
        final boolean success2 = sut.initialize(container2, connectionProvider);
        final boolean success3 = sut.initialize(container3, connectionProvider);

        // Assert
        assertThat(success1).isTrue();
        assertThat(success2).isFalse();
        assertThat(success3).isFalse();

        final Network expectedNetwork = container1.getNode().getNetwork();
        assertThat(expectedNetwork).isNotNull();

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
        assertThat(getAmountRemoved(expectedNetwork)).isZero();

        assertThat(unrelatedContainer.getNode().getNetwork()).isNotNull().isNotEqualTo(expectedNetwork);
        assertThat(unrelatedContainer.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactly(unrelatedContainer);
    }
}
