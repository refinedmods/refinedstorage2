package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RemoveNetworkBuilderImplTest extends AbstractNetworkBuilderImplTest {
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
}
