package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateNetworkBuilderImplTest extends AbstractNetworkBuilderImplTest {
    @Test
    void shouldNotBeAbleToUpdateWithoutNetworkAssigned() {
        // Arrange
        final NetworkNodeContainer container = createContainer();

        // Act
        final Executable action = () -> sut.update(container, new FakeConnectionProvider());

        // Assert
        assertThrows(IllegalStateException.class, action);
    }

    @Test
    void shouldUpdateWithSoleContainer() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        final NetworkNodeContainer container1 = createContainerWithNetwork();

        final Network originalNetwork = container1.getNode().getNetwork();

        final NetworkNodeContainer unrelatedContainer = createContainerWithNetwork();

        connectionProvider.with(container1, unrelatedContainer);

        // Reset the state so that we can track the update properly.
        clearTracking(container1.getNode().getNetwork());

        // Act
        sut.update(container1, connectionProvider);

        // Assert
        assertThat(container1.getNode().getNetwork())
            .isSameAs(originalNetwork)
            .isNotSameAs(unrelatedContainer.getNode().getNetwork())
            .isNotNull();

        assertThat(container1.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactly(container1);

        assertThat(getNetworkSplits(container1.getNode().getNetwork())).isEmpty();
        assertThat(getAddedContainers(container1.getNode().getNetwork())).isEmpty();
        assertThat(getRemovedContainers(container1.getNode().getNetwork())).isEmpty();
        assertThat(getAmountRemoved(container1.getNode().getNetwork())).isZero();
    }

    @Test
    void shouldSplitNetworkWhenUpdatingWithSoleContainerOnLeftSide() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        final NetworkNodeContainer container1 = createContainerWithNetwork();
        final NetworkNodeContainer container2 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());
        final NetworkNodeContainer container3 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());

        final Network originalNetwork = container1.getNode().getNetwork();

        final NetworkNodeContainer unrelatedContainer = createContainerWithNetwork();

        // Note: container 1 is no longer connected.
        connectionProvider
            .with(container1, container2, container3, unrelatedContainer)
            .connect(container2, container3);

        // Reset the state so that we can track the update properly.
        clearTracking(container1.getNode().getNetwork());

        // Act
        sut.update(container1, connectionProvider);

        // Assert
        assertThat(container1.getNode().getNetwork())
            .isSameAs(originalNetwork)
            .isNotSameAs(unrelatedContainer.getNode().getNetwork())
            .isNotNull();

        assertThat(container2.getNode().getNetwork())
            .isSameAs(container3.getNode().getNetwork())
            .isNotSameAs(unrelatedContainer.getNode().getNetwork())
            .isNotSameAs(container1.getNode().getNetwork())
            .isNotSameAs(originalNetwork)
            .isNotNull();

        assertThat(container1.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactly(container1);
        assertThat(container2.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactlyInAnyOrder(container2, container3);

        assertThat(getNetworkSplits(container1.getNode().getNetwork())).containsExactly(
            Set.of(container2.getNode().getNetwork())
        );
        assertThat(getAddedContainers(container1.getNode().getNetwork())).isEmpty();
        assertThat(getRemovedContainers(container1.getNode().getNetwork())).containsExactlyInAnyOrder(
            container2, container3
        );
        assertThat(getAmountRemoved(container1.getNode().getNetwork())).isZero();

        assertThat(getNetworkSplits(container2.getNode().getNetwork())).isEmpty();
        assertThat(getAddedContainers(container2.getNode().getNetwork())).containsExactlyInAnyOrder(
            container2, container3
        );
        assertThat(getRemovedContainers(container2.getNode().getNetwork())).isEmpty();
        assertThat(getAmountRemoved(container2.getNode().getNetwork())).isZero();
    }

    @Test
    void shouldSplitNetworkWhenUpdatingWithTwoContainersOnBothSides() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        final NetworkNodeContainer container1 = createContainerWithNetwork();
        final NetworkNodeContainer container2 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());
        final NetworkNodeContainer container3 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());
        final NetworkNodeContainer container4 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());

        final Network originalNetwork = container1.getNode().getNetwork();

        final NetworkNodeContainer unrelatedContainer = createContainerWithNetwork();

        connectionProvider
            .with(container1, container2, container3, container4, unrelatedContainer)
            .connect(container1, container2)
            .connect(container3, container4);

        // Reset the state so that we can track the update properly.
        clearTracking(container1.getNode().getNetwork());

        // Act
        sut.update(container1, connectionProvider);

        // Assert
        assertThat(container1.getNode().getNetwork())
            .isSameAs(container2.getNode().getNetwork())
            .isSameAs(originalNetwork)
            .isNotSameAs(unrelatedContainer.getNode().getNetwork())
            .isNotNull();

        assertThat(container3.getNode().getNetwork())
            .isSameAs(container4.getNode().getNetwork())
            .isNotSameAs(unrelatedContainer.getNode().getNetwork())
            .isNotSameAs(container1.getNode().getNetwork())
            .isNotSameAs(originalNetwork)
            .isNotNull();

        assertThat(container1.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactlyInAnyOrder(container1, container2);
        assertThat(container3.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactlyInAnyOrder(container3, container4);

        assertThat(getNetworkSplits(container1.getNode().getNetwork())).containsExactly(
            Set.of(container3.getNode().getNetwork())
        );
        assertThat(getAddedContainers(container1.getNode().getNetwork())).isEmpty();
        assertThat(getRemovedContainers(container1.getNode().getNetwork())).containsExactlyInAnyOrder(
            container3, container4
        );
        assertThat(getAmountRemoved(container1.getNode().getNetwork())).isZero();

        assertThat(getNetworkSplits(container3.getNode().getNetwork())).isEmpty();
        assertThat(getAddedContainers(container3.getNode().getNetwork())).containsExactlyInAnyOrder(
            container3, container4
        );
        assertThat(getRemovedContainers(container3.getNode().getNetwork())).isEmpty();
        assertThat(getAmountRemoved(container3.getNode().getNetwork())).isZero();
    }

    @Test
    void shouldSplitNetworkAndMergeAdditionalContainerWhenUpdating() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        final NetworkNodeContainer container1 = createContainerWithNetwork();
        final NetworkNodeContainer container2 = createContainerWithNetwork();
        final NetworkNodeContainer container3 =
            createContainerWithNetwork(container -> container2.getNode().getNetwork());
        final NetworkNodeContainer container4 =
            createContainerWithNetwork(container -> container2.getNode().getNetwork());

        final NetworkNodeContainer unrelatedContainer = createContainerWithNetwork();

        // Previous state: [[container1], [container2, container3, container4]]

        connectionProvider
            .with(container1, container2, container3, container4, unrelatedContainer)
            .connect(container1, container2)
            .connect(container3, container4);

        // Reset the state so that we can track the update properly.
        clearTracking(container1.getNode().getNetwork());
        clearTracking(container2.getNode().getNetwork());

        final Network originalNetworkContainer1 = container1.getNode().getNetwork();
        final Network originalNetworkContainer2 = container2.getNode().getNetwork();

        // Act
        sut.update(container2, connectionProvider);

        // Assert
        assertThat(container2.getNode().getNetwork())
            .isSameAs(container1.getNode().getNetwork())
            .isNotSameAs(unrelatedContainer.getNode().getNetwork())
            .isSameAs(originalNetworkContainer2)
            .isNotSameAs(originalNetworkContainer1)
            .isNotNull();

        assertThat(container3.getNode().getNetwork())
            .isSameAs(container4.getNode().getNetwork())
            .isNotSameAs(unrelatedContainer.getNode().getNetwork())
            .isNotSameAs(container2.getNode().getNetwork())
            .isNotSameAs(originalNetworkContainer2)
            .isNotNull();

        assertThat(container2.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactlyInAnyOrder(container1, container2);
        assertThat(container3.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactlyInAnyOrder(container3, container4);

        assertThat(getNetworkSplits(container2.getNode().getNetwork())).containsExactly(
            Set.of(container3.getNode().getNetwork())
        );
        assertThat(getAddedContainers(container2.getNode().getNetwork())).containsExactly(
            container1
        );
        assertThat(getRemovedContainers(container2.getNode().getNetwork())).containsExactlyInAnyOrder(
            container3, container4
        );
        assertThat(getAmountRemoved(container2.getNode().getNetwork())).isZero();

        assertThat(getNetworkSplits(container3.getNode().getNetwork())).isEmpty();
        assertThat(getAddedContainers(container3.getNode().getNetwork())).containsExactlyInAnyOrder(
            container3, container4
        );
        assertThat(getRemovedContainers(container3.getNode().getNetwork())).isEmpty();
        assertThat(getAmountRemoved(container3.getNode().getNetwork())).isZero();
    }

    @Test
    void shouldNotUpdateAnythingWhenStateIsTheSame() {
        // Arrange
        final FakeConnectionProvider connectionProvider = new FakeConnectionProvider();

        final NetworkNodeContainer container1 = createContainerWithNetwork();
        final NetworkNodeContainer container2 =
            createContainerWithNetwork(container -> container1.getNode().getNetwork());

        final NetworkNodeContainer container3 = createContainerWithNetwork();
        final NetworkNodeContainer container4 =
            createContainerWithNetwork(container -> container3.getNode().getNetwork());

        final Network originalNetworkContainer1 = container1.getNode().getNetwork();
        final Network originalNetworkContainer3 = container3.getNode().getNetwork();

        final NetworkNodeContainer unrelatedContainer = createContainerWithNetwork();

        connectionProvider
            .with(container1, container2, container3, container4, unrelatedContainer)
            .connect(container1, container2)
            .connect(container3, container4);

        // Reset the state so that we can track the update properly.
        clearTracking(container1.getNode().getNetwork());
        clearTracking(container3.getNode().getNetwork());

        // Act
        sut.update(container1, connectionProvider);

        // Assert
        assertThat(container1.getNode().getNetwork())
            .isSameAs(container2.getNode().getNetwork())
            .isSameAs(originalNetworkContainer1)
            .isNotSameAs(unrelatedContainer.getNode().getNetwork())
            .isNotSameAs(originalNetworkContainer3)
            .isNotNull();

        assertThat(container3.getNode().getNetwork())
            .isSameAs(container4.getNode().getNetwork())
            .isSameAs(originalNetworkContainer3)
            .isNotSameAs(unrelatedContainer.getNode().getNetwork())
            .isNotSameAs(originalNetworkContainer1)
            .isNotNull();

        assertThat(container1.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactlyInAnyOrder(container1, container2);
        assertThat(container3.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getContainers())
            .containsExactlyInAnyOrder(container3, container4);

        assertThat(getNetworkSplits(container1.getNode().getNetwork())).isEmpty();
        assertThat(getAddedContainers(container1.getNode().getNetwork())).isEmpty();
        assertThat(getRemovedContainers(container1.getNode().getNetwork())).isEmpty();
        assertThat(getAmountRemoved(container1.getNode().getNetwork())).isZero();

        assertThat(getNetworkSplits(container3.getNode().getNetwork())).isEmpty();
        assertThat(getAddedContainers(container3.getNode().getNetwork())).isEmpty();
        assertThat(getRemovedContainers(container3.getNode().getNetwork())).isEmpty();
        assertThat(getAmountRemoved(container3.getNode().getNetwork())).isZero();
    }
}
