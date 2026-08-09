package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.network.impl.NetworkImpl;
import com.refinedmods.refinedstorage.api.network.node.GraphListener;
import com.refinedmods.refinedstorage.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.network.test.fixtures.NetworkTestFixtures;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphNetworkComponentImplTest {
    GraphNetworkComponent sut;

    @BeforeEach
    void setUp() {
        sut = new GraphNetworkComponentImpl(new NetworkImpl(NetworkTestFixtures.NETWORK_COMPONENT_MAP_FACTORY));
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
    void shouldNotRetrieveContainersByClassThatDoesNotExist() {
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
    void shouldAddAndRetrieveMultipleContainersByInterface() {
        // Arrange
        final NetworkNodeContainer1 container11 = new NetworkNodeContainer1();
        final NetworkNodeContainer1 container12 = new NetworkNodeContainer1();
        final NetworkNodeContainer2 container2 = new NetworkNodeContainer2();
        sut.onContainerAdded(container11);
        sut.onContainerAdded(container12);
        sut.onContainerAdded(container2);

        // Act
        final Set<BothImplements> containers = sut.getContainers(BothImplements.class);

        // Assert
        assertThat(containers).containsExactlyInAnyOrder(container11, container12, container2);
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
    void shouldRemoveSingleContainerAndRetrieveByInterface() {
        // Arrange
        final NetworkNodeContainer1 container11 = new NetworkNodeContainer1();
        final NetworkNodeContainer1 container12 = new NetworkNodeContainer1();
        final NetworkNodeContainer2 container2 = new NetworkNodeContainer2();
        sut.onContainerAdded(container11);
        sut.onContainerAdded(container12);
        sut.onContainerAdded(container2);

        // Act
        sut.onContainerRemoved(container12);

        final Set<BothImplements> containers = sut.getContainers(BothImplements.class);

        // Assert
        assertThat(containers).containsExactlyInAnyOrder(container11, container2);
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
        final Set<BothImplements> containersByIface = sut.getContainers(BothImplements.class);

        // Assert
        assertThat(containers1).isEmpty();
        assertThat(containers2).containsExactly(container2);
        assertThat(containersByIface).containsExactly(container2);
    }

    @Test
    void shouldRemoveMultipleContainersAndRetrieveByInterface() {
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
        sut.onContainerRemoved(container2);

        final Set<BothImplements> containers = sut.getContainers(BothImplements.class);

        // Assert
        assertThat(containers).isEmpty();
    }

    @Test
    void shouldNotRetrieveContainerByIndexThatDoesNotExist() {
        // Arrange
        sut.onContainerAdded(() -> new SimpleNetworkNode(0));

        // Act
        final NetworkNodeContainer container = sut.getContainer("does not exist");

        // Assert
        assertThat(container).isNull();
    }

    @Test
    void shouldRetrieveContainerByIndex() {
        // Arrange
        final NetworkNodeContainer container1 = new NetworkNodeContainer() {
            @Override
            public NetworkNode getNode() {
                return new SimpleNetworkNode(0);
            }

            @Override
            public Object createKey() {
                return "key0";
            }
        };

        final NetworkNodeContainer container2 = new NetworkNodeContainer() {
            @Override
            public NetworkNode getNode() {
                return new SimpleNetworkNode(0);
            }

            @Override
            public Object createKey() {
                return "key1";
            }
        };

        sut.onContainerAdded(container1);
        sut.onContainerAdded(container2);

        // Act
        final NetworkNodeContainer foundContainer1 = sut.getContainer("key0");
        final NetworkNodeContainer foundContainer2 = sut.getContainer("key1");

        // Assert
        assertThat(foundContainer1).isEqualTo(container1);
        assertThat(foundContainer2).isEqualTo(container2);
    }

    @Test
    void shouldRemoveContainerWithKey() {
        // Arrange
        final NetworkNodeContainer container1 = new NetworkNodeContainer() {
            @Override
            public NetworkNode getNode() {
                return new SimpleNetworkNode(0);
            }

            @Override
            public Object createKey() {
                return "key0";
            }
        };

        final NetworkNodeContainer container2 = new NetworkNodeContainer() {
            @Override
            public NetworkNode getNode() {
                return new SimpleNetworkNode(0);
            }

            @Override
            public Object createKey() {
                return "key1";
            }
        };

        sut.onContainerAdded(container1);
        sut.onContainerAdded(container2);

        // Act
        sut.onContainerRemoved(container2);

        // Assert
        final NetworkNodeContainer foundContainer1 = sut.getContainer("key0");
        final NetworkNodeContainer foundContainer2 = sut.getContainer("key1");

        assertThat(foundContainer1).isEqualTo(container1);
        assertThat(foundContainer2).isNull();
    }

    @Test
    void shouldNotifyListenerWhenContainerIsAdded() {
        // Arrange
        final RecordingGraphListener listener = new RecordingGraphListener();
        sut.addListener(listener);

        final NetworkNodeContainer container1 = () -> new SimpleNetworkNode(0);
        final NetworkNodeContainer container2 = () -> new SimpleNetworkNode(0);

        // Act
        sut.onContainerAdded(container1);
        sut.onContainerAdded(container2);

        // Assert
        assertThat(listener.added).containsExactly(container1, container2);
        assertThat(listener.removed).isEmpty();
    }

    @Test
    void shouldNotifyListenerWhenContainerIsRemoved() {
        // Arrange
        final RecordingGraphListener listener = new RecordingGraphListener();
        sut.addListener(listener);

        final NetworkNodeContainer container1 = () -> new SimpleNetworkNode(0);
        final NetworkNodeContainer container2 = () -> new SimpleNetworkNode(0);
        sut.onContainerAdded(container1);
        sut.onContainerAdded(container2);

        // Act
        sut.onContainerRemoved(container1);

        // Assert
        assertThat(listener.added).containsExactly(container1, container2);
        assertThat(listener.removed).containsExactly(container1);
    }

    @Test
    void shouldNotifyMultipleListeners() {
        // Arrange
        final RecordingGraphListener listener1 = new RecordingGraphListener();
        final RecordingGraphListener listener2 = new RecordingGraphListener();
        sut.addListener(listener1);
        sut.addListener(listener2);

        final NetworkNodeContainer container = () -> new SimpleNetworkNode(0);

        // Act
        sut.onContainerAdded(container);
        sut.onContainerRemoved(container);

        // Assert
        assertThat(listener1.added).containsExactly(container);
        assertThat(listener1.removed).containsExactly(container);
        assertThat(listener2.added).containsExactly(container);
        assertThat(listener2.removed).containsExactly(container);
    }

    @Test
    void shouldNotNotifyListenerAfterRemoval() {
        // Arrange
        final RecordingGraphListener listener = new RecordingGraphListener();
        sut.addListener(listener);

        final NetworkNodeContainer container1 = () -> new SimpleNetworkNode(0);
        sut.onContainerAdded(container1);

        // Act
        sut.removeListener(listener);

        final NetworkNodeContainer container2 = () -> new SimpleNetworkNode(0);
        sut.onContainerAdded(container2);
        sut.onContainerRemoved(container1);

        // Assert
        assertThat(listener.added).containsExactly(container1);
        assertThat(listener.removed).isEmpty();
    }

    private static class RecordingGraphListener implements GraphListener {
        private final List<NetworkNodeContainer> added = new ArrayList<>();
        private final List<NetworkNodeContainer> removed = new ArrayList<>();

        @Override
        public void onContainerAdded(final NetworkNodeContainer container) {
            added.add(container);
        }

        @Override
        public void onContainerRemoved(final NetworkNodeContainer container) {
            removed.add(container);
        }
    }

    private static class NetworkNodeContainer1 implements NetworkNodeContainer, BothImplements {
        @Override
        public NetworkNode getNode() {
            return new SimpleNetworkNode(0);
        }
    }

    private static class NetworkNodeContainer2 implements NetworkNodeContainer, BothImplements {
        @Override
        public NetworkNode getNode() {
            return new SimpleNetworkNode(0);
        }
    }

    private interface BothImplements {
    }
}
