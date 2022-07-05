package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.network.test.NetworkTestFixtures;
import com.refinedmods.refinedstorage2.api.network.test.SpyingNetworkNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NetworkBuilderTest {
    ComponentMapFactory<NetworkComponent, Network> componentMapFactory;
    NetworkBuilder sut;

    @BeforeEach
    void setUp() {
        componentMapFactory = NetworkTestFixtures.NETWORK_COMPONENT_MAP_FACTORY.copy();
        componentMapFactory.addFactory(InterceptingNetworkComponent.class,
            network -> new InterceptingNetworkComponent());
        sut = new NetworkBuilder(new NetworkFactory(componentMapFactory));
    }

    @Test
    void shouldFormNetwork() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        final NetworkNodeContainer container = createContainer();
        final NetworkNodeContainer unrelatedContainer = createContainerWithNetwork();

        connectionProvider.with(container, unrelatedContainer);

        // Act
        sut.initialize(container, connectionProvider);

        // Assert
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
        sut.initialize(newContainer, connectionProvider);

        // Assert
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
        sut.initialize(newContainer, connectionProvider);

        // Assert
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
        sut.initialize(container1, connectionProvider);
        sut.initialize(container2, connectionProvider);
        sut.initialize(container3, connectionProvider);

        // Assert
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

    @Test
    void shouldNotBeAbleToRemoveWithoutNetworkAssigned() {
        // Arrange
        final NetworkNodeContainer container = createContainer();

        // Act
        final Executable action = () -> sut.remove(container, new FakeConnectionProvider());

        // Assert
        assertThrows(IllegalStateException.class, action);
    }

    @Test
    void shouldSplitNetwork() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        final NetworkNodeContainer container1 = createContainerWithNetwork();
        final NetworkNodeContainer container2 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());
        final NetworkNodeContainer container3 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());

        final NetworkNodeContainer unrelatedContainer = createContainerWithNetwork();

        connectionProvider
            .with(container1, container2, container3, unrelatedContainer)
            .connect(container2, container3);

        // Act
        sut.remove(container1, connectionProvider);

        // Assert
        assertThat(container1.getNode().getNetwork()).isNull();

        assertThat(container2.getNode().getNetwork())
            .isSameAs(container3.getNode().getNetwork())
            .isNotSameAs(unrelatedContainer.getNode().getNetwork())
            .isNotNull();

        assertThat(container2.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactlyInAnyOrder(container2, container3);

        assertThat(getNetworkSplits(container2.getNode().getNetwork())).isEmpty();
        assertThat(getAddedContainers(container2.getNode().getNetwork())).containsExactly(
            container1,
            container2,
            container3
        );
        assertThat(getRemovedContainers(container2.getNode().getNetwork())).containsExactly(container1);
        assertThat(getAmountRemoved(container2.getNode().getNetwork())).isZero();
    }

    @Test
    void shouldSplitNetworkInTwo() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        final NetworkNodeContainer container1 = createContainerWithNetwork();
        final NetworkNodeContainer container2 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());

        final NetworkNodeContainer container3 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());

        final NetworkNodeContainer container4 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());
        final NetworkNodeContainer container5 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());

        final NetworkNodeContainer unrelatedContainer = createContainerWithNetwork();

        connectionProvider
            .with(container5, container4, container3, container2, container1, unrelatedContainer)
            .connect(container1, container2)
            .connect(container4, container5);

        // Act
        sut.remove(container3, connectionProvider);

        // Assert
        assertThat(container3.getNode().getNetwork()).isNull();

        assertThat(container1.getNode().getNetwork())
            .isSameAs(container2.getNode().getNetwork())
            .isNotSameAs(unrelatedContainer.getNode().getNetwork())
            .isNotSameAs(container4.getNode().getNetwork())
            .isNotSameAs(container5.getNode().getNetwork());

        assertThat(container1.getNode().getNetwork()).isNotNull();
        assertThat(getNetworkSplits(container1.getNode().getNetwork())).isEmpty();
        assertThat(getAddedContainers(container1.getNode().getNetwork()))
            .containsExactlyInAnyOrder(container1, container2);
        assertThat(getRemovedContainers(container1.getNode().getNetwork())).isEmpty();
        assertThat(getAmountRemoved(container1.getNode().getNetwork())).isZero();

        assertThat(container1.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactlyInAnyOrder(container1, container2);

        assertThat(container4.getNode().getNetwork())
            .isSameAs(container5.getNode().getNetwork())
            .isNotSameAs(unrelatedContainer.getNode().getNetwork())
            .isNotSameAs(container1.getNode().getNetwork())
            .isNotSameAs(container2.getNode().getNetwork())
            .isNotNull();

        final List<Set<Network>> splits = getNetworkSplits(container4.getNode().getNetwork());
        assertThat(splits).hasSize(1);
        assertThat(splits.get(0)).containsExactlyInAnyOrder(container1.getNode().getNetwork());

        assertThat(getAddedContainers(container4.getNode().getNetwork()))
            .containsExactlyInAnyOrder(container1, container2, container3, container4, container5);
        assertThat(getRemovedContainers(container4.getNode().getNetwork()))
            .containsExactlyInAnyOrder(container1, container2, container3);
        assertThat(getAmountRemoved(container4.getNode().getNetwork())).isZero();

        assertThat(container4.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactlyInAnyOrder(container4, container5);
    }

    @Test
    void shouldSplitNetworkInThree() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        final NetworkNodeContainer container1 = createContainerWithNetwork();

        final NetworkNodeContainer container2 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());

        final NetworkNodeContainer container3 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());

        final NetworkNodeContainer container4 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());
        final NetworkNodeContainer container5 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());

        final NetworkNodeContainer unrelatedContainer = createContainerWithNetwork();

        connectionProvider
            .with(unrelatedContainer, container5, container4, container3, container2, container1)
            .connect(container4, container5);

        // Act
        sut.remove(container1, connectionProvider);

        // Assert
        assertThat(container1.getNode().getNetwork()).isNull();

        assertThat(container2.getNode().getNetwork())
            .isNotSameAs(container3.getNode().getNetwork())
            .isNotSameAs(container4.getNode().getNetwork())
            .isNotSameAs(container5.getNode().getNetwork())
            .isNotSameAs(unrelatedContainer.getNode().getNetwork())
            .isNotNull();

        assertThat(getNetworkSplits(container2.getNode().getNetwork())).isEmpty();
        assertThat(getAddedContainers(container2.getNode().getNetwork())).containsExactlyInAnyOrder(container2);
        assertThat(getRemovedContainers(container2.getNode().getNetwork())).isEmpty();
        assertThat(getAmountRemoved(container2.getNode().getNetwork())).isZero();

        assertThat(container3.getNode().getNetwork())
            .isNotSameAs(container2.getNode().getNetwork())
            .isNotSameAs(container4.getNode().getNetwork())
            .isNotSameAs(container5.getNode().getNetwork())
            .isNotSameAs(unrelatedContainer.getNode().getNetwork())
            .isNotNull();

        assertThat(getNetworkSplits(container3.getNode().getNetwork())).isEmpty();
        assertThat(getAddedContainers(container3.getNode().getNetwork())).containsExactlyInAnyOrder(container3);
        assertThat(getRemovedContainers(container3.getNode().getNetwork())).isEmpty();
        assertThat(getAmountRemoved(container3.getNode().getNetwork())).isZero();

        assertThat(container4.getNode().getNetwork())
            .isNotSameAs(container2.getNode().getNetwork())
            .isNotSameAs(container3.getNode().getNetwork())
            .isSameAs(container5.getNode().getNetwork())
            .isNotSameAs(unrelatedContainer.getNode().getNetwork())
            .isNotNull();

        final List<Set<Network>> splits = getNetworkSplits(container4.getNode().getNetwork());
        assertThat(splits).hasSize(1);
        assertThat(splits.get(0))
            .containsExactlyInAnyOrder(container2.getNode().getNetwork(), container3.getNode().getNetwork());

        assertThat(getAddedContainers(container4.getNode().getNetwork()))
            .containsExactlyInAnyOrder(container1, container2, container3, container4, container5);
        assertThat(getRemovedContainers(container4.getNode().getNetwork()))
            .containsExactlyInAnyOrder(container1, container2, container3);
        assertThat(getAmountRemoved(container4.getNode().getNetwork())).isZero();

        assertThat(container2.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactly(container2);

        assertThat(container3.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactly(container3);

        assertThat(container4.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactlyInAnyOrder(container4, container5);
    }

    @Test
    void shouldRemoveNetwork() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        final NetworkNodeContainer container = createContainerWithNetwork();
        final NetworkNodeContainer unrelatedContainer = createContainerWithNetwork();

        connectionProvider.with(container, unrelatedContainer);

        final Network network = container.getNode().getNetwork();

        // Act
        sut.remove(container, connectionProvider);

        // Assert
        assertThat(container.getNode().getNetwork()).isNull();

        assertThat(unrelatedContainer.getNode().getNetwork()).isNotNull();
        assertThat(unrelatedContainer.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactlyInAnyOrder(unrelatedContainer);

        assertThat(network).isNotNull();
        assertThat(getNetworkSplits(network)).isEmpty();
        assertThat(getAddedContainers(network)).containsExactly(container);
        assertThat(getRemovedContainers(network)).isEmpty();
        assertThat(getAmountRemoved(network)).isEqualTo(1);
    }

    private NetworkNodeContainer createContainerWithNetwork() {
        return createContainerWithNetwork(container -> new NetworkImpl(componentMapFactory));
    }

    private static NetworkNodeContainer createContainerWithNetwork(
        final Function<NetworkNodeContainer, Network> networkFactory) {
        final NetworkNodeContainer container = createContainer();
        final Network network = networkFactory.apply(container);
        container.getNode().setNetwork(network);
        network.addContainer(container);
        return container;
    }

    private static NetworkNodeContainer createContainer() {
        final SpyingNetworkNode node = new SpyingNetworkNode(0);
        return () -> node;
    }

    private static List<NetworkNodeContainer> getAddedContainers(final Network network) {
        return network.getComponent(InterceptingNetworkComponent.class).added;
    }

    private static List<NetworkNodeContainer> getRemovedContainers(final Network network) {
        return network.getComponent(InterceptingNetworkComponent.class).removed;
    }

    private static List<Set<Network>> getNetworkSplits(final Network network) {
        return network.getComponent(InterceptingNetworkComponent.class).splits;
    }

    private static List<Network> getNetworkMerges(final Network network) {
        return network.getComponent(InterceptingNetworkComponent.class).merges;
    }

    private static int getAmountRemoved(final Network network) {
        return network.getComponent(InterceptingNetworkComponent.class).amountRemoved;
    }

    private static class InterceptingNetworkComponent implements NetworkComponent {
        private final List<NetworkNodeContainer> added = new ArrayList<>();
        private final List<NetworkNodeContainer> removed = new ArrayList<>();
        private final List<Set<Network>> splits = new ArrayList<>();
        private final List<Network> merges = new ArrayList<>();
        private int amountRemoved = 0;

        @Override
        public void onContainerAdded(final NetworkNodeContainer container) {
            added.add(container);
        }

        @Override
        public void onContainerRemoved(final NetworkNodeContainer container) {
            removed.add(container);
        }

        @Override
        public void onNetworkRemoved() {
            amountRemoved++;
        }

        @Override
        public void onNetworkSplit(final Set<Network> networks) {
            splits.add(networks);
        }

        @Override
        public void onNetworkMergedWith(final Network newMainNetwork) {
            merges.add(newMainNetwork);
        }
    }
}
