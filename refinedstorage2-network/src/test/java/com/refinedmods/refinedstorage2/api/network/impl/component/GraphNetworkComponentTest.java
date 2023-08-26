package com.refinedmods.refinedstorage2.api.network.impl.component;

import com.refinedmods.refinedstorage2.api.network.impl.NetworkImpl;
import com.refinedmods.refinedstorage2.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphNetworkComponentTest {
    GraphNetworkComponent sut;

    @BeforeEach
    void setUp() {
        sut = new GraphNetworkComponent(new NetworkImpl(NetworkTestFixtures.NETWORK_COMPONENT_MAP_FACTORY));
    }

    @Test
    void shouldAddContainer() {
        // Arrange
        final NetworkNodeContainer container1 = () -> new SimpleNetworkNode(0);
        final NetworkNodeContainer container2 = () -> new SimpleNetworkNode(0);

        // Act
        sut.onContainerAdded(container1);
        sut.onContainerAdded(container2);

        // Assert
        assertThat(sut.getContainers()).containsExactlyInAnyOrder(container1, container2);
    }

    @Test
    void shouldRemoveContainer() {
        // Arrange
        final NetworkNodeContainer container1 = () -> new SimpleNetworkNode(0);
        final NetworkNodeContainer container2 = () -> new SimpleNetworkNode(0);
        sut.onContainerAdded(container1);
        sut.onContainerAdded(container2);

        // Act
        sut.onContainerRemoved(container1);

        // Assert
        assertThat(sut.getContainers()).containsExactly(container2);
    }

    @Test
    void shouldNotRetrieveContainersByClassThatDontExist() {
        // Act
        final Set<NetworkNodeContainer1> containers = sut.getContainers(NetworkNodeContainer1.class);

        // Assert
        assertThat(containers).isEmpty();
    }

    @Test
    void shouldAddAndRetrieveSingleContainerByClass() {
        // Arrange
        final NetworkNodeContainer1 container1 = new NetworkNodeContainer1();
        final NetworkNodeContainer2 container2 = new NetworkNodeContainer2();
        sut.onContainerAdded(container1);
        sut.onContainerAdded(container2);

        // Act
        final Set<NetworkNodeContainer1> containers = sut.getContainers(NetworkNodeContainer1.class);

        // Assert
        assertThat(containers).containsExactly(container1);
    }

    @Test
    void shouldAddAndRetrieveMultipleContainersByClass() {
        // Arrange
        final NetworkNodeContainer1 container11 = new NetworkNodeContainer1();
        final NetworkNodeContainer1 container12 = new NetworkNodeContainer1();
        final NetworkNodeContainer2 container2 = new NetworkNodeContainer2();
        sut.onContainerAdded(container11);
        sut.onContainerAdded(container12);
        sut.onContainerAdded(container2);

        // Act
        final Set<NetworkNodeContainer1> containers = sut.getContainers(NetworkNodeContainer1.class);

        // Assert
        assertThat(containers).containsExactlyInAnyOrder(container11, container12);
    }

    @Test
    void shouldRemoveSingleContainerAndRetrieveByClass() {
        // Arrange
        final NetworkNodeContainer1 container11 = new NetworkNodeContainer1();
        final NetworkNodeContainer1 container12 = new NetworkNodeContainer1();
        final NetworkNodeContainer2 container2 = new NetworkNodeContainer2();
        sut.onContainerAdded(container11);
        sut.onContainerAdded(container12);
        sut.onContainerAdded(container2);

        // Act
        sut.onContainerRemoved(container12);

        final Set<NetworkNodeContainer1> containers = sut.getContainers(NetworkNodeContainer1.class);

        // Assert
        assertThat(containers).containsExactly(container11);
    }

    @Test
    void shouldRemoveMultipleContainersAndRetrieveByClass() {
        // Arrange
        final NetworkNodeContainer1 container11 = new NetworkNodeContainer1();
        final NetworkNodeContainer1 container12 = new NetworkNodeContainer1();
        final NetworkNodeContainer2 container2 = new NetworkNodeContainer2();
        sut.onContainerAdded(container11);
        sut.onContainerAdded(container12);
        sut.onContainerAdded(container2);

        // Act
        sut.onContainerRemoved(container11);
        sut.onContainerRemoved(container12);

        final Set<NetworkNodeContainer1> containers1 = sut.getContainers(NetworkNodeContainer1.class);
        final Set<NetworkNodeContainer2> containers2 = sut.getContainers(NetworkNodeContainer2.class);

        // Assert
        assertThat(containers1).isEmpty();
        assertThat(containers2).containsExactly(container2);
    }

    private static class NetworkNodeContainer1 implements NetworkNodeContainer {
        @Override
        public NetworkNode getNode() {
            return new SimpleNetworkNode(0);
        }
    }

    private static class NetworkNodeContainer2 implements NetworkNodeContainer {
        @Override
        public NetworkNode getNode() {
            return new SimpleNetworkNode(0);
        }
    }
}
