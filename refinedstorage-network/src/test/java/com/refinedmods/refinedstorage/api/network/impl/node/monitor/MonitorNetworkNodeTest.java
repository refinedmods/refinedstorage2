package com.refinedmods.refinedstorage.api.network.impl.node.monitor;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.NetworkNodeDetailsChangedEvent;
import com.refinedmods.refinedstorage.api.network.impl.node.NetworkNodeEventManager;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetailsProvider;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeListener;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeType;
import com.refinedmods.refinedstorage.api.network.node.StorageNetworkNodeDetailsProvider;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.composite.PriorityProvider;
import com.refinedmods.refinedstorage.network.test.InjectNetwork;
import com.refinedmods.refinedstorage.network.test.InjectNetworkGraphComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.RecordingNetworkNodeListener;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@NetworkTest
@SetupNetwork
@SetupNetwork(id = "other")
@SetupNetwork(id = "energy", energyStored = 50, energyCapacity = 100)
class MonitorNetworkNodeTest {
    @InjectNetwork
    Network network;

    @InjectNetwork("other")
    Network otherNetwork;

    @InjectNetwork("energy")
    Network energyNetwork;

    MonitorNetworkNode sut;

    @BeforeEach
    void setUp() {
        sut = new MonitorNetworkNode(0);
        sut.setEnergyUsage(5);
        sut.setNetwork(network);
    }

    @Test
    void testInitialState() {
        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(5);
        assertThat(sut.getNodes()).isEmpty();
        assertThat(sut.getTypes()).isEmpty();
        assertThat(sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE)).isNull();
        assertThat(sut.getNodeCount()).isZero();
    }

    @Test
    void shouldTrackNodeAddedToGraph(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final NetworkNode node = new NetworkNodeWithDetailsAndType();
        final NetworkNodeContainer container = () -> node;

        // Act
        graph.onContainerAdded(container);

        // Assert
        assertThat(sut.getNodes()).containsExactly(node);
        assertThat(sut.getNodeCount()).isEqualTo(1);

        final MonitorNodeId id = sut.getId(node);
        assertThat(id).isNotNull();
        assertThat(sut.getNode(id)).isSameAs(node);
        assertThat(sut.getContainer(id)).isSameAs(container);
        assertThat(sut.getDetailsProvider(id)).isSameAs(node);
    }

    @Test
    void shouldNotReturnAnythingForUnknownId() {
        // Act & assert
        final MonitorNodeId unknownId = MonitorNodeId.create();
        assertThat(sut.getNode(unknownId)).isNull();
        assertThat(sut.getContainer(unknownId)).isNull();
        assertThat(sut.getDetailsProvider(unknownId)).isNull();
    }

    @Test
    void shouldNotTrackSameNodeTwice(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final NetworkNode node = new NetworkNodeWithDetailsAndType();
        final NetworkNodeContainer container = () -> node;
        final NetworkNodeContainer duplicateContainer = () -> node;

        // Act
        graph.onContainerAdded(container);
        graph.onContainerAdded(duplicateContainer);

        // Assert
        assertThat(sut.getNodeCount()).isEqualTo(1);
        assertThat(sut.getNodes(NetworkNodeWithDetailsAndType.TYPE)).containsExactly(node);

        // The container of the first tracking wins.
        final MonitorNodeId id = sut.getId(node);
        assertThat(id).isNotNull();
        assertThat(sut.getContainer(id)).isSameAs(container);
        assertThat(sut.getDetailsProvider(id)).isSameAs(node);
    }

    @Test
    void shouldTrackNodesByType(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final NetworkNode node1 = new NetworkNodeWithDetailsAndType();
        final NetworkNode node2 = new NetworkNodeWithDetailsAndType();
        final NetworkNode node3 = new NetworkNodeWithDetailsAndType();

        // Act
        graph.onContainerAdded(() -> node1);
        graph.onContainerAdded(() -> node2);
        graph.onContainerAdded(() -> node3);

        // Assert
        assertThat(sut.getNodes()).containsExactlyInAnyOrder(node1, node2, node3);
        assertThat(sut.getNodes(NetworkNodeWithDetailsAndType.TYPE)).containsExactlyInAnyOrder(node1, node2, node3);
        assertThat(sut.getTypes()).containsExactly(NetworkNodeWithDetailsAndType.TYPE);
    }

    @Test
    void shouldTrackTypeById(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final NetworkNode node = new NetworkNodeWithDetailsAndType();

        // Act
        graph.onContainerAdded(() -> node);

        // Assert
        final MonitorNodeTypeId typeId = sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE);
        assertThat(typeId).isNotNull();
        assertThat(sut.getType(typeId)).isSameAs(NetworkNodeWithDetailsAndType.TYPE);
    }

    @Test
    void shouldReuseTypeIdForNodesOfTheSameType(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final NetworkNode node1 = new NetworkNodeWithDetailsAndType();
        final NetworkNode node2 = new NetworkNodeWithDetailsAndType();
        graph.onContainerAdded(() -> node1);
        final MonitorNodeTypeId typeId = sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE);

        // Act
        graph.onContainerAdded(() -> node2);

        // Assert
        assertThat(typeId).isNotNull();
        assertThat(sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE)).isEqualTo(typeId);
    }

    @Test
    void shouldTrackDifferentTypesWithDifferentIds(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final NetworkNode node = new NetworkNodeWithDetailsAndType(NetworkNodeWithDetailsAndType.TYPE);
        final NetworkNode otherNode = new NetworkNodeWithDetailsAndType(NetworkNodeWithDetailsAndType.OTHER_TYPE);

        // Act
        graph.onContainerAdded(() -> node);
        graph.onContainerAdded(() -> otherNode);

        // Assert
        assertThat(sut.getTypes()).containsExactlyInAnyOrder(
            NetworkNodeWithDetailsAndType.TYPE,
            NetworkNodeWithDetailsAndType.OTHER_TYPE
        );
        assertThat(sut.getNodes(NetworkNodeWithDetailsAndType.TYPE)).containsExactly(node);
        assertThat(sut.getNodes(NetworkNodeWithDetailsAndType.OTHER_TYPE)).containsExactly(otherNode);

        final MonitorNodeTypeId typeId = sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE);
        final MonitorNodeTypeId otherTypeId = sut.getTypeId(NetworkNodeWithDetailsAndType.OTHER_TYPE);
        assertThat(typeId).isNotNull();
        assertThat(otherTypeId).isNotNull().isNotEqualTo(typeId);
        assertThat(sut.getType(typeId)).isSameAs(NetworkNodeWithDetailsAndType.TYPE);
        assertThat(sut.getType(otherTypeId)).isSameAs(NetworkNodeWithDetailsAndType.OTHER_TYPE);
    }

    @Test
    void shouldNotReturnAnythingForUnknownTypeId() {
        // Act & assert
        assertThat(sut.getType(MonitorNodeTypeId.create())).isNull();
    }

    @Test
    void shouldNotTrackNodeThatIsNotADetailsProvider(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNode node = new NetworkNodeWithoutDetailsAndType();

        // Act
        graph.onContainerAdded(() -> node);

        // Assert
        assertThat(sut.getNodes()).isEmpty();
        assertThat(sut.getNodeCount()).isZero();
        assertThat(sut.getId(node)).isNull();
        assertThat(sut.getTypes()).isEmpty();
    }

    @Test
    void shouldStopTrackingRemovedNode(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final NetworkNode node1 = new NetworkNodeWithDetailsAndType();
        final NetworkNode node2 = new NetworkNodeWithDetailsAndType();
        final NetworkNodeContainer container1 = () -> node1;
        final NetworkNodeContainer container2 = () -> node2;
        graph.onContainerAdded(container1);
        graph.onContainerAdded(container2);
        final MonitorNodeId id1 = sut.getId(node1);

        // Act
        graph.onContainerRemoved(container1);

        // Assert
        assertThat(sut.getNodes()).containsExactly(node2);
        assertThat(sut.getNodes(NetworkNodeWithDetailsAndType.TYPE)).containsExactly(node2);
        assertThat(sut.getId(node1)).isNull();
        assertThat(id1).isNotNull();
        assertThat(sut.getNode(id1)).isNull();
        assertThat(sut.getContainer(id1)).isNull();
        assertThat(sut.getDetailsProvider(id1)).isNull();

        final MonitorNodeId id2 = sut.getId(node2);
        assertThat(id2).isNotNull();
        assertThat(sut.getContainer(id2)).isSameAs(container2);
        assertThat(sut.getDetailsProvider(id2)).isSameAs(node2);
    }

    @Test
    void shouldRemoveTypeGroupWhenLastNodeOfTypeIsRemoved(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNode node = new NetworkNodeWithDetailsAndType();
        final NetworkNodeContainer container = () -> node;
        graph.onContainerAdded(container);
        final MonitorNodeTypeId typeId = sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE);
        assertThat(typeId).isNotNull();

        // Act
        graph.onContainerRemoved(container);

        // Assert
        assertThat(sut.getTypes()).isEmpty();
        assertThat(sut.getNodes(NetworkNodeWithDetailsAndType.TYPE)).isEmpty();
        assertThat(sut.getNodeCount()).isZero();
        assertThat(sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE)).isNull();
        assertThat(sut.getType(typeId)).isNull();
    }

    @Test
    void shouldKeepTypeIdWhenAnotherNodeOfTypeRemains(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNode node1 = new NetworkNodeWithDetailsAndType();
        final NetworkNode node2 = new NetworkNodeWithDetailsAndType();
        final NetworkNodeContainer container1 = () -> node1;
        graph.onContainerAdded(container1);
        graph.onContainerAdded(() -> node2);
        final MonitorNodeTypeId typeId = sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE);
        assertThat(typeId).isNotNull();

        // Act
        graph.onContainerRemoved(container1);

        // Assert
        assertThat(sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE)).isEqualTo(typeId);
        assertThat(sut.getType(typeId)).isSameAs(NetworkNodeWithDetailsAndType.TYPE);
        assertThat(sut.getNodes(NetworkNodeWithDetailsAndType.TYPE)).containsExactly(node2);
    }

    @Test
    void shouldIgnoreRemovalOfUntrackedNode(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final NetworkNode tracked = new NetworkNodeWithDetailsAndType();
        graph.onContainerAdded(() -> tracked);

        // Act
        graph.onContainerRemoved(NetworkNodeWithDetailsAndType::new);

        // Assert
        assertThat(sut.getNodes()).containsExactly(tracked);
    }

    @Test
    void shouldTrackExistingNodesWhenAttachingToNetwork(
        @InjectNetworkGraphComponent(networkId = "other") final GraphNetworkComponent otherGraph
    ) {
        // Arrange - add nodes to the graph before the monitor attaches to it
        final NetworkNode existing1 = new NetworkNodeWithDetailsAndType();
        final NetworkNode existing2 = new NetworkNodeWithDetailsAndType();
        final NetworkNodeContainer container1 = () -> existing1;
        final NetworkNodeContainer container2 = () -> existing2;
        otherGraph.onContainerAdded(container1);
        otherGraph.onContainerAdded(container2);

        // Act
        sut.setNetwork(otherNetwork);

        // Assert
        assertThat(sut.getNodes()).containsExactlyInAnyOrder(existing1, existing2);
        assertThat(sut.getNodes(NetworkNodeWithDetailsAndType.TYPE)).containsExactlyInAnyOrder(existing1, existing2);
        final MonitorNodeId id1 = sut.getId(existing1);
        final MonitorNodeId id2 = sut.getId(existing2);
        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(sut.getContainer(id1)).isSameAs(container1);
        assertThat(sut.getContainer(id2)).isSameAs(container2);
        assertThat(sut.getDetailsProvider(id1)).isSameAs(existing1);
        assertThat(sut.getDetailsProvider(id2)).isSameAs(existing2);
    }

    @Test
    void shouldStopTrackingAndDetachWhenNetworkIsRemoved(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNode node = new NetworkNodeWithDetailsAndType();
        graph.onContainerAdded(() -> node);
        assertThat(sut.getNodeCount()).isEqualTo(1);
        final MonitorNodeId id = sut.getId(node);
        assertThat(id).isNotNull();
        final MonitorNodeTypeId typeId = sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE);
        assertThat(typeId).isNotNull();

        // Act
        sut.setNetwork(null);

        // Assert
        assertThat(sut.getNodes()).isEmpty();
        assertThat(sut.getTypes()).isEmpty();
        assertThat(sut.getId(node)).isNull();
        assertThat(sut.getContainer(id)).isNull();
        assertThat(sut.getDetailsProvider(id)).isNull();
        assertThat(sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE)).isNull();
        assertThat(sut.getType(typeId)).isNull();

        // The listener is detached, so the old network is no longer observed.
        graph.onContainerAdded(NetworkNodeWithDetailsAndType::new);
        assertThat(sut.getNodes()).isEmpty();
    }

    @Test
    void shouldTrackFromNewNetworkAfterNetworkChanges(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph,
        @InjectNetworkGraphComponent(networkId = "other") final GraphNetworkComponent otherGraph
    ) {
        // Arrange
        final NetworkNode oldNode = new NetworkNodeWithDetailsAndType();
        graph.onContainerAdded(() -> oldNode);
        assertThat(sut.getNodes()).containsExactly(oldNode);
        final MonitorNodeTypeId oldTypeId = sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE);
        assertThat(oldTypeId).isNotNull();

        // Act
        sut.setNetwork(otherNetwork);

        // Assert
        assertThat(sut.getNodes()).isEmpty();
        assertThat(sut.getId(oldNode)).isNull();
        assertThat(sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE)).isNull();

        // The old network is no longer observed.
        graph.onContainerAdded(NetworkNodeWithDetailsAndType::new);
        assertThat(sut.getNodes()).isEmpty();

        // The new network is observed.
        final NetworkNode newNode = new NetworkNodeWithDetailsAndType();
        otherGraph.onContainerAdded(() -> newNode);
        assertThat(sut.getNodes()).containsExactly(newNode);
        final MonitorNodeTypeId newTypeId = sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE);
        assertThat(newTypeId).isNotNull().isNotEqualTo(oldTypeId);
        assertThat(sut.getType(newTypeId)).isSameAs(NetworkNodeWithDetailsAndType.TYPE);
        assertThat(sut.getType(oldTypeId)).isNull();
    }

    @Test
    void shouldNotifyListenerWhenNodeIsTracked(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final RecordingMonitorListener listener = new RecordingMonitorListener();
        sut.addListener(listener);
        final NetworkNode node = new NetworkNodeWithDetailsAndType(NetworkNodeWithDetailsAndType.TYPE);
        final NetworkNode otherNode = new NetworkNodeWithDetailsAndType(NetworkNodeWithDetailsAndType.OTHER_TYPE);

        // Act
        graph.onContainerAdded(() -> node);
        graph.onContainerAdded(() -> otherNode);

        // Assert
        final MonitorNodeId id = sut.getId(node);
        final MonitorNodeId otherId = sut.getId(otherNode);
        final MonitorNodeTypeId typeId = sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE);
        final MonitorNodeTypeId otherTypeId = sut.getTypeId(NetworkNodeWithDetailsAndType.OTHER_TYPE);
        assertThat(id).isNotNull();
        assertThat(otherId).isNotNull();
        assertThat(typeId).isNotNull();
        assertThat(otherTypeId).isNotNull();
        assertThat(listener.tracked).containsExactly(
            new TrackedNode(id, typeId),
            new TrackedNode(otherId, otherTypeId)
        );
        assertThat(listener.untracked).isEmpty();
    }

    @Test
    void shouldNotifyListenerWithSameTypeIdForNodesOfSameType(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final RecordingMonitorListener listener = new RecordingMonitorListener();
        sut.addListener(listener);
        final NetworkNode node1 = new NetworkNodeWithDetailsAndType();
        final NetworkNode node2 = new NetworkNodeWithDetailsAndType();

        // Act
        graph.onContainerAdded(() -> node1);
        graph.onContainerAdded(() -> node2);

        // Assert
        final MonitorNodeTypeId typeId = sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE);
        assertThat(typeId).isNotNull();
        assertThat(listener.tracked).extracting(TrackedNode::typeId).containsExactly(typeId, typeId);
    }

    @Test
    void shouldNotifyListenerWhenNodeIsUntracked(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final NetworkNode node = new NetworkNodeWithDetailsAndType();
        final NetworkNodeContainer container = () -> node;
        graph.onContainerAdded(container);
        final MonitorNodeId id = sut.getId(node);
        assertThat(id).isNotNull();
        final RecordingMonitorListener listener = new RecordingMonitorListener();
        sut.addListener(listener);

        // Act
        graph.onContainerRemoved(container);

        // Assert
        assertThat(listener.tracked).isEmpty();
        assertThat(listener.untracked).containsExactly(id);
    }

    @Test
    void shouldNotNotifyListenerOfNodeThatIsNotADetailsProvider(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final RecordingMonitorListener listener = new RecordingMonitorListener();
        sut.addListener(listener);
        final NetworkNode node = new NetworkNodeWithoutDetailsAndType();
        final NetworkNodeContainer container = () -> node;

        // Act
        graph.onContainerAdded(container);
        graph.onContainerRemoved(container);

        // Assert
        assertThat(sut.getNodes()).isEmpty();
        assertThat(listener.tracked).isEmpty();
        assertThat(listener.untracked).isEmpty();
    }

    @Test
    void shouldNotNotifyListenerOfNodesTrackedBeforeSubscribing(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNode node = new NetworkNodeWithDetailsAndType();
        graph.onContainerAdded(() -> node);

        // Act
        final RecordingMonitorListener listener = new RecordingMonitorListener();
        sut.addListener(listener);

        // Assert
        assertThat(sut.getNodes()).containsExactly(node);
        assertThat(listener.tracked).isEmpty();
        assertThat(listener.untracked).isEmpty();
    }

    @Test
    void shouldNotNotifyRemovedListener(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final NetworkNode node = new NetworkNodeWithDetailsAndType();
        final NetworkNodeContainer container = () -> node;
        final RecordingMonitorListener listener = new RecordingMonitorListener();
        sut.addListener(listener);
        sut.removeListener(listener);

        // Act
        graph.onContainerAdded(container);
        graph.onContainerRemoved(container);

        // Assert
        assertThat(listener.tracked).isEmpty();
        assertThat(listener.untracked).isEmpty();
    }

    @Test
    void shouldNotifyListenerWhenNetworkChanges(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph,
        @InjectNetworkGraphComponent(networkId = "other") final GraphNetworkComponent otherGraph
    ) {
        // Arrange
        final NetworkNode oldNode = new NetworkNodeWithDetailsAndType();
        graph.onContainerAdded(() -> oldNode);
        final NetworkNode newNode = new NetworkNodeWithDetailsAndType();
        otherGraph.onContainerAdded(() -> newNode);
        final MonitorNodeId oldId = sut.getId(oldNode);
        assertThat(oldId).isNotNull();
        final RecordingMonitorListener listener = new RecordingMonitorListener();
        sut.addListener(listener);

        // Act
        sut.setNetwork(otherNetwork);

        // Assert
        final MonitorNodeId newId = sut.getId(newNode);
        final MonitorNodeTypeId newTypeId = sut.getTypeId(NetworkNodeWithDetailsAndType.TYPE);
        assertThat(newId).isNotNull();
        assertThat(newTypeId).isNotNull();
        assertThat(listener.untracked).containsExactly(oldId);
        assertThat(listener.tracked).containsExactly(new TrackedNode(newId, newTypeId));
    }

    @Test
    void shouldNotifyNodeListenerWhenNodeChanges(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final NetworkNodeWithDetailsAndType node = new NetworkNodeWithDetailsAndType();
        graph.onContainerAdded(() -> node);
        final MonitorNodeId id = sut.getId(node);
        assertThat(id).isNotNull();
        final RecordingNetworkNodeListener listener = new RecordingNetworkNodeListener();

        // Act
        sut.addNodeListener(id, listener);
        node.setEnergyUsage(3);
        node.setActive(true);

        // Assert
        assertThat(listener.events).containsExactly(
            new NetworkNodeDetailsChangedEvent(3, false),
            new NetworkNodeDetailsChangedEvent(3, true)
        );
    }

    @Test
    void shouldNotNotifyNodeListenerOfChangesBeforeSubscribing(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNodeWithDetailsAndType node = new NetworkNodeWithDetailsAndType();
        graph.onContainerAdded(() -> node);
        final MonitorNodeId id = sut.getId(node);
        assertThat(id).isNotNull();
        node.setEnergyUsage(3);

        // Act
        final RecordingNetworkNodeListener listener = new RecordingNetworkNodeListener();
        sut.addNodeListener(id, listener);

        // Assert
        assertThat(listener.events).isEmpty();
    }

    @Test
    void shouldNotNotifyNodeListenerOfChangesInOtherNodes(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNodeWithDetailsAndType node = new NetworkNodeWithDetailsAndType();
        final NetworkNodeWithDetailsAndType otherNode = new NetworkNodeWithDetailsAndType();
        graph.onContainerAdded(() -> node);
        graph.onContainerAdded(() -> otherNode);
        final MonitorNodeId id = sut.getId(node);
        assertThat(id).isNotNull();
        final RecordingNetworkNodeListener listener = new RecordingNetworkNodeListener();
        sut.addNodeListener(id, listener);

        // Act
        otherNode.setEnergyUsage(3);
        otherNode.setActive(true);

        // Assert
        assertThat(listener.events).isEmpty();
    }

    @Test
    void shouldIgnoreNodeListenerForUnknownNode(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final NetworkNodeWithDetailsAndType node = new NetworkNodeWithDetailsAndType();
        graph.onContainerAdded(() -> node);
        final RecordingNetworkNodeListener listener = new RecordingNetworkNodeListener();

        // Act
        sut.addNodeListener(MonitorNodeId.create(), listener);
        node.setEnergyUsage(3);

        // Assert
        assertThat(listener.events).isEmpty();
    }

    @Test
    void shouldRemoveNodeListener(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final NetworkNodeWithDetailsAndType node = new NetworkNodeWithDetailsAndType();
        graph.onContainerAdded(() -> node);
        final MonitorNodeId id = sut.getId(node);
        assertThat(id).isNotNull();
        final RecordingNetworkNodeListener listener = new RecordingNetworkNodeListener();
        final RecordingNetworkNodeListener otherListener = new RecordingNetworkNodeListener();
        sut.addNodeListener(id, listener);
        sut.addNodeListener(id, otherListener);

        // Act
        sut.removeNodeListener(id, listener);
        node.setEnergyUsage(3);

        // Assert
        assertThat(listener.events).isEmpty();
        assertThat(otherListener.events).containsExactly(new NetworkNodeDetailsChangedEvent(3, false));
    }

    @Test
    void shouldKeepTrackingRemainingNodeListeners(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final NetworkNodeWithDetailsAndType node = new NetworkNodeWithDetailsAndType();
        final NetworkNodeContainer container = () -> node;
        graph.onContainerAdded(container);
        final MonitorNodeId id = sut.getId(node);
        assertThat(id).isNotNull();
        final RecordingNetworkNodeListener listener = new RecordingNetworkNodeListener();
        final RecordingNetworkNodeListener otherListener = new RecordingNetworkNodeListener();
        sut.addNodeListener(id, listener);
        sut.addNodeListener(id, otherListener);
        sut.removeNodeListener(id, listener);

        // Act
        graph.onContainerRemoved(container);
        node.setEnergyUsage(3);

        // Assert
        assertThat(listener.events).isEmpty();
        assertThat(otherListener.events).isEmpty();
    }

    @Test
    void shouldIgnoreRemovalOfNodeListenerThatIsNotListening(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNodeWithDetailsAndType node = new NetworkNodeWithDetailsAndType();
        graph.onContainerAdded(() -> node);
        final MonitorNodeId id = sut.getId(node);
        assertThat(id).isNotNull();
        final RecordingNetworkNodeListener listener = new RecordingNetworkNodeListener();
        sut.addNodeListener(id, listener);

        // Act & assert
        assertDoesNotThrow(() -> sut.removeNodeListener(id, new RecordingNetworkNodeListener()));
        assertDoesNotThrow(() -> sut.removeNodeListener(MonitorNodeId.create(), listener));

        node.setEnergyUsage(3);
        assertThat(listener.events).containsExactly(new NetworkNodeDetailsChangedEvent(3, false));
    }

    @Test
    void shouldRemoveNodeListenerWhenNodeIsNoLongerMonitored(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNodeWithDetailsAndType node = new NetworkNodeWithDetailsAndType();
        final NetworkNodeContainer container = () -> node;
        graph.onContainerAdded(container);
        final MonitorNodeId id = sut.getId(node);
        assertThat(id).isNotNull();
        final RecordingNetworkNodeListener listener = new RecordingNetworkNodeListener();
        sut.addNodeListener(id, listener);

        // Act
        graph.onContainerRemoved(container);
        node.setEnergyUsage(3);

        // Assert
        assertThat(listener.events).isEmpty();
    }

    @Test
    void shouldRemoveNodeListenerWhenNetworkChanges(@InjectNetworkGraphComponent final GraphNetworkComponent graph) {
        // Arrange
        final NetworkNodeWithDetailsAndType node = new NetworkNodeWithDetailsAndType();
        graph.onContainerAdded(() -> node);
        final MonitorNodeId id = sut.getId(node);
        assertThat(id).isNotNull();
        final RecordingNetworkNodeListener listener = new RecordingNetworkNodeListener();
        sut.addNodeListener(id, listener);

        // Act
        sut.setNetwork(otherNetwork);
        node.setEnergyUsage(3);

        // Assert
        assertThat(listener.events).isEmpty();
    }

    @Test
    void shouldNotifyListenerOfActivenessChanges() {
        // Arrange
        final RecordingMonitorListener listener = new RecordingMonitorListener();
        sut.addListener(listener);

        // Act
        sut.setActive(true);
        sut.setActive(false);

        // Assert
        assertThat(listener.activeChanges).containsExactly(true, false);
    }

    @Test
    void shouldNotNotifyRemovedListenerOfActivenessChanges() {
        // Arrange
        final RecordingMonitorListener listener = new RecordingMonitorListener();
        sut.addListener(listener);
        sut.removeListener(listener);

        // Act
        sut.setActive(true);

        // Assert
        assertThat(listener.activeChanges).isEmpty();
    }

    @Test
    void shouldReturnZeroTotalEnergyUsageWhenNoNodesAreTracked() {
        // Act & assert
        assertThat(sut.getTotalEnergyUsage()).isZero();
    }

    @Test
    void shouldCalculateTotalEnergyUsageFromTrackedNodes(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNodeWithDetailsAndType node1 = new NetworkNodeWithDetailsAndType();
        node1.setEnergyUsage(5);
        final NetworkNodeWithDetailsAndType node2 = new NetworkNodeWithDetailsAndType();
        node2.setEnergyUsage(10);

        // Act
        graph.onContainerAdded(() -> node1);
        graph.onContainerAdded(() -> node2);

        // Assert
        assertThat(sut.getTotalEnergyUsage()).isEqualTo(15);
    }

    @Test
    void shouldIgnoreNodesWithoutDetailsWhenCalculatingTotalEnergyUsage(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNodeWithDetailsAndType node = new NetworkNodeWithDetailsAndType();
        node.setEnergyUsage(5);
        final NetworkNodeWithoutDetailsAndType nodeWithoutDetails = new NetworkNodeWithoutDetailsAndType();

        // Act
        graph.onContainerAdded(() -> node);
        graph.onContainerAdded(() -> nodeWithoutDetails);

        // Assert
        assertThat(sut.getTotalEnergyUsage()).isEqualTo(5);
    }

    @Test
    void shouldUpdateTotalEnergyUsageWhenNodeEnergyUsageChanges(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNodeWithDetailsAndType node = new NetworkNodeWithDetailsAndType();
        node.setEnergyUsage(5);
        graph.onContainerAdded(() -> node);
        assertThat(sut.getTotalEnergyUsage()).isEqualTo(5);

        // Act
        node.setEnergyUsage(20);

        // Assert
        assertThat(sut.getTotalEnergyUsage()).isEqualTo(20);
    }

    @Test
    void shouldReturnZeroTotalEnergyUsageWhenNetworkIsRemoved(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNodeWithDetailsAndType node = new NetworkNodeWithDetailsAndType();
        node.setEnergyUsage(5);
        graph.onContainerAdded(() -> node);

        // Act
        sut.setNetwork(null);

        // Assert
        assertThat(sut.getTotalEnergyUsage()).isZero();
    }

    @Test
    void shouldReturnEnergyStoredAndCapacityFromNetwork() {
        // Act
        sut.setNetwork(energyNetwork);

        // Assert
        assertThat(sut.getEnergyStored()).isEqualTo(50);
        assertThat(sut.getEnergyCapacity()).isEqualTo(100);
    }

    @Test
    void shouldReturnZeroEnergyStoredAndCapacityWhenNoNetwork() {
        // Act
        sut.setNetwork(null);

        // Assert
        assertThat(sut.getEnergyStored()).isZero();
        assertThat(sut.getEnergyCapacity()).isZero();
    }

    @Test
    void shouldNotTrackAnyStoragesInitially() {
        // Act & assert
        assertThat(sut.getStorages()).isEmpty();
    }

    @Test
    void shouldIndexStorageNetworkNodeDetailsProviders(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNodeWithStorageDetails storageNode1 = new NetworkNodeWithStorageDetails();
        final NetworkNodeWithStorageDetails storageNode2 = new NetworkNodeWithStorageDetails();
        final NetworkNodeWithDetailsAndType nonStorageNode = new NetworkNodeWithDetailsAndType();

        // Act
        graph.onContainerAdded(() -> storageNode1);
        graph.onContainerAdded(() -> storageNode2);
        graph.onContainerAdded(() -> nonStorageNode);

        // Assert
        assertThat(sut.getStorages()).containsExactlyInAnyOrder(storageNode1, storageNode2);
    }

    @Test
    void shouldStopIndexingStorageProviderWhenNodeIsRemoved(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNodeWithStorageDetails storageNode1 = new NetworkNodeWithStorageDetails();
        final NetworkNodeWithStorageDetails storageNode2 = new NetworkNodeWithStorageDetails();
        final NetworkNodeContainer container1 = () -> storageNode1;
        graph.onContainerAdded(container1);
        graph.onContainerAdded(() -> storageNode2);

        // Act
        graph.onContainerRemoved(container1);

        // Assert
        assertThat(sut.getStorages()).containsExactly(storageNode2);
    }

    @Test
    void shouldStopIndexingStorageProvidersWhenNetworkIsRemoved(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        graph.onContainerAdded(NetworkNodeWithStorageDetails::new);

        // Act
        sut.setNetwork(null);

        // Assert
        assertThat(sut.getStorages()).isEmpty();
    }

    @Test
    void shouldReturnZeroStoredAndCapacityWhenNoStorageProvidersAreTracked() {
        // Act & assert
        assertThat(sut.getStored(storage -> true)).isZero();
        assertThat(sut.getCapacity(storage -> true)).isZero();
    }

    @Test
    void shouldCalculateStoredAndCapacityFromTrackedStorageProviders(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNodeWithStorageDetails storageNode1 = new NetworkNodeWithStorageDetails();
        storageNode1.setStored(10);
        storageNode1.setCapacity(100);
        final NetworkNodeWithStorageDetails storageNode2 = new NetworkNodeWithStorageDetails();
        storageNode2.setStored(20);
        storageNode2.setCapacity(50);

        // Act
        graph.onContainerAdded(() -> storageNode1);
        graph.onContainerAdded(() -> storageNode2);

        // Assert
        assertThat(sut.getStored(storage -> true)).isEqualTo(30);
        assertThat(sut.getCapacity(storage -> true)).isEqualTo(150);
    }

    @Test
    void shouldIgnoreNonStorageProvidersWhenCalculatingStoredAndCapacity(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNodeWithStorageDetails storageNode = new NetworkNodeWithStorageDetails();
        storageNode.setStored(10);
        storageNode.setCapacity(100);
        final NetworkNodeWithDetailsAndType nonStorageNode = new NetworkNodeWithDetailsAndType();

        // Act
        graph.onContainerAdded(() -> storageNode);
        graph.onContainerAdded(() -> nonStorageNode);

        // Assert
        assertThat(sut.getStored(storage -> true)).isEqualTo(10);
        assertThat(sut.getCapacity(storage -> true)).isEqualTo(100);
    }

    @Test
    void shouldStopCountingStoredAndCapacityForRemovedStorageProvider(
        @InjectNetworkGraphComponent final GraphNetworkComponent graph
    ) {
        // Arrange
        final NetworkNodeWithStorageDetails storageNode1 = new NetworkNodeWithStorageDetails();
        storageNode1.setStored(10);
        storageNode1.setCapacity(100);
        final NetworkNodeWithStorageDetails storageNode2 = new NetworkNodeWithStorageDetails();
        storageNode2.setStored(20);
        storageNode2.setCapacity(50);
        final NetworkNodeContainer container1 = () -> storageNode1;
        graph.onContainerAdded(container1);
        graph.onContainerAdded(() -> storageNode2);

        // Act
        graph.onContainerRemoved(container1);

        // Assert
        assertThat(sut.getStored(storage -> true)).isEqualTo(20);
        assertThat(sut.getCapacity(storage -> true)).isEqualTo(50);
    }

    private record TrackedNode(MonitorNodeId id, MonitorNodeTypeId typeId) {
    }

    private static final class RecordingMonitorListener implements MonitorListener {
        private final List<TrackedNode> tracked = new ArrayList<>();
        private final List<MonitorNodeId> untracked = new ArrayList<>();
        private final List<Boolean> activeChanges = new ArrayList<>();

        @Override
        public void onNodeTracked(final MonitorNodeId id, final MonitorNodeTypeId typeId) {
            tracked.add(new TrackedNode(id, typeId));
        }

        @Override
        public void onNodeUntracked(final MonitorNodeId id) {
            untracked.add(id);
        }

        @Override
        public void onActiveChanged(final boolean newActive) {
            activeChanges.add(newActive);
        }
    }

    private static final class NetworkNodeWithDetailsAndType extends AbstractNetworkNode
        implements NetworkNodeDetailsProvider {
        private static final NetworkNodeType TYPE = new NetworkNodeType() {
        };
        private static final NetworkNodeType OTHER_TYPE = new NetworkNodeType() {
        };

        private final NetworkNodeEventManager eventManager = new NetworkNodeEventManager();
        private final NetworkNodeType type;
        private long energyUsage;

        private NetworkNodeWithDetailsAndType() {
            this(TYPE);
        }

        private NetworkNodeWithDetailsAndType(final NetworkNodeType type) {
            this.type = type;
        }

        private void setEnergyUsage(final long energyUsage) {
            this.energyUsage = energyUsage;
            eventManager.notifyDetailsChanged(energyUsage, isActive());
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        protected void onActiveChanged(final boolean newActive) {
            eventManager.notifyDetailsChanged(energyUsage, newActive);
        }

        @Override
        public void addListener(final NetworkNodeListener listener) {
            eventManager.addListener(listener);
        }

        @Override
        public void removeListener(final NetworkNodeListener listener) {
            eventManager.removeListener(listener);
        }

        @Override
        public NetworkNodeType getType() {
            return type;
        }

        @Override
        public NetworkNodeDetails createDetails() {
            return SimpleNetworkNodeDetails.of(this);
        }
    }

    private static final class NetworkNodeWithStorageDetails extends AbstractNetworkNode
        implements StorageNetworkNodeDetailsProvider {
        private static final NetworkNodeType TYPE = new NetworkNodeType() {
        };

        private final NetworkNodeEventManager eventManager = new NetworkNodeEventManager();
        private long stored;
        private long capacity;

        private void setStored(final long stored) {
            this.stored = stored;
        }

        private void setCapacity(final long capacity) {
            this.capacity = capacity;
        }

        @Override
        public long getStored(final Predicate<Storage> storageFilter) {
            return stored;
        }

        @Override
        public long getCapacity(final Predicate<Storage> storageFilter) {
            return capacity;
        }

        @Override
        @Nullable
        public PriorityProvider getPriority() {
            return null;
        }

        @Override
        public long getEnergyUsage() {
            return 0;
        }

        @Override
        public void addListener(final NetworkNodeListener listener) {
            eventManager.addListener(listener);
        }

        @Override
        public void removeListener(final NetworkNodeListener listener) {
            eventManager.removeListener(listener);
        }

        @Override
        public NetworkNodeType getType() {
            return TYPE;
        }

        @Override
        public NetworkNodeDetails createDetails() {
            return SimpleNetworkNodeDetails.of(this);
        }
    }

    private static final class NetworkNodeWithoutDetailsAndType implements NetworkNode {
        @Nullable
        private Network network;

        @Override
        @Nullable
        public Network getNetwork() {
            return network;
        }

        @Override
        public void setNetwork(@Nullable final Network network) {
            this.network = network;
        }
    }
}
