package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.graph.GraphScanner;
import com.refinedmods.refinedstorage2.core.graph.GraphScannerResult;
import com.refinedmods.refinedstorage2.core.network.node.HidingNetworkNodeRepository;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeRepository;
import com.refinedmods.refinedstorage2.core.network.node.graph.NetworkNodeRequest;
import com.refinedmods.refinedstorage2.core.network.node.graph.NetworkNodeRequestHandler;
import com.refinedmods.refinedstorage2.core.util.Direction;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkManagerImpl implements NetworkManager {
    private static final Logger LOGGER = LogManager.getLogger(NetworkManagerImpl.class);

    private final GraphScanner<NetworkNode, NetworkNodeRequest> graphScanner;
    private final Map<UUID, Network> networks = new HashMap<>();

    public NetworkManagerImpl() {
        this.graphScanner = new GraphScanner<>(new NetworkNodeRequestHandler());
    }

    @Override
    public Network onNodeAdded(NetworkNodeRepository nodeRepository, Position pos) {
        if (!nodeRepository.getNode(pos).isPresent()) {
            throw new NetworkManagerException(String.format("Could not find added node at position %s", pos));
        }

        LOGGER.debug("A node has been added at {}", pos);

        Set<Network> neighboringNetworks = getNeighboringNetworks(nodeRepository, pos);
        if (neighboringNetworks.isEmpty()) {
            return formNetwork(nodeRepository, pos);
        } else {
            return mergeNetworks(nodeRepository, neighboringNetworks, pos);
        }
    }

    private Network mergeNetworks(NetworkNodeRepository nodeRepository, Set<Network> neighboringNetworks, Position pos) {
        GraphScannerResult<NetworkNode> result = graphScanner.scanAt(new NetworkNodeRequest(nodeRepository, pos));

        Iterator<Network> it = neighboringNetworks.iterator();
        Network pivotNetwork = it.next();
        while (it.hasNext()) {
            Network network = it.next();
            removeNetwork(network);
            LOGGER.debug("Merged network {} with {}", network.getId(), pivotNetwork.getId());
        }

        pivotNetwork.getNodeReferences().clear();
        result.getAllEntries().forEach(node -> {
            node.setNetwork(pivotNetwork);
            pivotNetwork.getNodeReferences().add(node.createReference());
            LOGGER.debug("Changing network of node {} to {}", node.getPosition(), pivotNetwork.getId());
        });

        pivotNetwork.onNodesChanged();

        return pivotNetwork;
    }

    private Network formNetwork(NetworkNodeRepository nodeRepository, Position pos) {
        Network network = new NetworkImpl(UUID.randomUUID());
        addNetwork(network);

        GraphScannerResult<NetworkNode> result = graphScanner.scanAt(new NetworkNodeRequest(nodeRepository, pos));

        result.getAllEntries().forEach(node -> {
            node.setNetwork(network);
            network.getNodeReferences().add(node.createReference());
        });

        network.onNodesChanged();

        LOGGER.debug("Formed new network {} with {} references", network.getId(), network.getNodeReferences().size());

        return network;
    }

    @Override
    public void onNodeRemoved(NetworkNodeRepository nodeRepository, Position pos) {
        NetworkNode node = nodeRepository.getNode(pos).orElseThrow(() -> new NetworkManagerException(String.format("The node at %s is not present", pos)));

        for (Network neighboringNetwork : getNeighboringNetworks(nodeRepository, pos)) {
            if (neighboringNetwork != node.getNetwork()) {
                throw new NetworkManagerException(String.format("The network manager was left in invalid state. The network of a neighboring node doesn't match the origin node. The origin node is located at %s", pos));
            }
        }

        Optional<NetworkNode> neighborNode = getFirstNeighboringNode(nodeRepository, pos);
        if (neighborNode.isPresent()) {
            splitNetworks(nodeRepository, neighborNode.get(), pos);
        } else {
            removeNetwork(node.getNetwork());
        }
    }

    private void splitNetworks(NetworkNodeRepository nodeRepository, NetworkNode pivot, Position removedPos) {
        Network pivotNetwork = pivot.getNetwork();
        Set<NetworkNode> pivotNodes = getNodesInNetwork(pivotNetwork);

        LOGGER.debug("Splitting network {}", pivotNetwork.getId());

        nodeRepository = new HidingNetworkNodeRepository(nodeRepository, removedPos);

        GraphScannerResult<NetworkNode> result = graphScanner.scanAt(new NetworkNodeRequest(nodeRepository, pivot.getPosition()), pivotNodes);

        LOGGER.debug("Network {} retains {} references", pivotNetwork.getId(), result.getAllEntries().size());

        pivotNetwork.getNodeReferences().clear();
        for (NetworkNode node : result.getAllEntries()) {
            pivotNetwork.getNodeReferences().add(node.createReference());
        }

        pivotNetwork.onNodesChanged();

        LOGGER.debug("Network {} has lost {} references, forming new networks where necessary", pivotNetwork.getId(), result.getRemovedEntries().size());

        for (NetworkNode removedNode : result.getRemovedEntries()) {
            removedNode.setNetwork(null);
        }

        boolean foundRemovedNode = false;

        for (NetworkNode removedNode : result.getRemovedEntries()) {
            if (removedNode.getPosition().equals(removedPos)) {
                foundRemovedNode = true;
                continue;
            }

            if (removedNode.getNetwork() == null) {
                LOGGER.debug("Forming new network at {}", removedNode.getPosition());
                formNetwork(nodeRepository, removedNode.getPosition());
            }
        }

        if (!foundRemovedNode) {
            throw new NetworkManagerException(String.format("The node that was removed at %s wasn't marked as a removed node in the graph", removedPos));
        }
    }

    private Set<NetworkNode> getNodesInNetwork(Network network) {
        return network.getNodeReferences()
                .stream()
                .map(NetworkNodeReference::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private Optional<NetworkNode> getFirstNeighboringNode(NetworkNodeRepository nodeRepository, Position pos) {
        for (Direction dir : Direction.values()) {
            Position offsetPos = pos.offset(dir);

            Optional<NetworkNode> node = nodeRepository.getNode(offsetPos);
            if (node.isPresent()) {
                return node;
            }
        }

        return Optional.empty();
    }

    private Set<Network> getNeighboringNetworks(NetworkNodeRepository nodeRepository, Position pos) {
        Set<Network> neighboringNetworks = new HashSet<>();
        for (Direction dir : Direction.values()) {
            Position offsetPos = pos.offset(dir);

            nodeRepository.getNode(offsetPos).ifPresent(node -> {
                Network network = node.getNetwork();
                if (network == null) {
                    throw new NetworkManagerException(String.format("The network manager was left in an invalid state. Network node at %s has no network!", offsetPos));
                }

                neighboringNetworks.add(network);
            });
        }

        return neighboringNetworks;
    }

    private void removeNetwork(Network network) {
        LOGGER.debug("Network {} has been removed", network.getId());
        networks.remove(network.getId());
    }

    public void addNetwork(Network network) {
        LOGGER.debug("Network {} has been added", network.getId());
        networks.put(network.getId(), network);
    }

    @Override
    public Collection<Network> getNetworks() {
        return networks.values();
    }

    @Override
    public void update() {
        networks.values().forEach(Network::update);
    }
}
