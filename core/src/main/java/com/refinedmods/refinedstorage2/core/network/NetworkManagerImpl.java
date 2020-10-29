package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.graph.GraphScanner;
import com.refinedmods.refinedstorage2.core.graph.GraphScannerResult;
import com.refinedmods.refinedstorage2.core.network.node.HidingNetworkNodeAdapter;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeAdapter;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.graph.NetworkNodeRequest;
import com.refinedmods.refinedstorage2.core.network.node.graph.NetworkNodeRequestHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class NetworkManagerImpl implements NetworkManager {
    private static final Logger LOGGER = LogManager.getLogger(NetworkManagerImpl.class);

    private final GraphScanner<NetworkNode, NetworkNodeRequest> graphScanner;
    private final Map<UUID, Network> networks = new HashMap<>();

    public NetworkManagerImpl() {
        this.graphScanner = new GraphScanner<>(new NetworkNodeRequestHandler());
    }

    @Override
    public Network onNodeAdded(NetworkNodeAdapter nodeAdapter, BlockPos pos) {
        if (!nodeAdapter.getNode(pos).isPresent()) {
            throw new NetworkManagerException(String.format("Could not find added node at position %s", pos));
        }

        LOGGER.debug("A node has been added at {}", pos);

        Set<Network> neighboringNetworks = getNeighboringNetworks(nodeAdapter, pos);
        if (neighboringNetworks.isEmpty()) {
            return formNetwork(nodeAdapter, pos);
        } else {
            return mergeNetworks(nodeAdapter, neighboringNetworks, pos);
        }
    }

    private Network mergeNetworks(NetworkNodeAdapter nodeAdapter, Set<Network> neighboringNetworks, BlockPos pos) {
        GraphScannerResult<NetworkNode> result = graphScanner.scanAt(new NetworkNodeRequest(nodeAdapter, pos));

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

        return pivotNetwork;
    }

    private Network formNetwork(NetworkNodeAdapter nodeAdapter, BlockPos pos) {
        Network network = new NetworkImpl(UUID.randomUUID());
        addNetwork(network);

        GraphScannerResult<NetworkNode> result = graphScanner.scanAt(new NetworkNodeRequest(nodeAdapter, pos));

        result.getAllEntries().forEach(node -> {
            node.setNetwork(network);
            network.getNodeReferences().add(node.createReference());
        });

        LOGGER.debug("Formed new network {} with {} references", network.getId(), network.getNodeReferences().size());

        return network;
    }

    @Override
    public void onNodeRemoved(NetworkNodeAdapter nodeAdapter, BlockPos pos) {
        NetworkNode node = nodeAdapter.getNode(pos).orElseThrow(() -> new NetworkManagerException(String.format("The node at %s is not present", pos)));

        for (Network neighboringNetwork : getNeighboringNetworks(nodeAdapter, pos)) {
            if (neighboringNetwork != node.getNetwork()) {
                throw new NetworkManagerException(String.format("The network manager was left in invalid state. The network of a neighboring node doesn't match the origin node. The origin node is located at %s", pos));
            }
        }

        Optional<NetworkNode> neighborNode = getFirstNeighboringNode(nodeAdapter, pos);
        if (neighborNode.isPresent()) {
            splitNetworks(nodeAdapter, neighborNode.get(), pos);
        } else {
            removeNetwork(node.getNetwork());
        }
    }

    private void splitNetworks(NetworkNodeAdapter nodeAdapter, NetworkNode pivot, BlockPos removedPos) {
        Network pivotNetwork = pivot.getNetwork();
        Set<NetworkNode> pivotNodes = getNodesInNetwork(pivotNetwork);

        LOGGER.debug("Splitting network {}", pivotNetwork.getId());

        nodeAdapter = new HidingNetworkNodeAdapter(nodeAdapter, removedPos);

        GraphScannerResult<NetworkNode> result = graphScanner.scanAt(new NetworkNodeRequest(nodeAdapter, pivot.getPosition()), pivotNodes);

        LOGGER.debug("Network {} retains {} references", pivotNetwork.getId(), result.getAllEntries().size());

        pivotNetwork.getNodeReferences().clear();
        for (NetworkNode node : result.getAllEntries()) { // TODO ensure that NetworkNode#network is set again.
            pivotNetwork.getNodeReferences().add(node.createReference());
        }

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
                formNetwork(nodeAdapter, removedNode.getPosition());
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

    private Optional<NetworkNode> getFirstNeighboringNode(NetworkNodeAdapter nodeAdapter, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos offsetPos = pos.offset(dir);

            Optional<NetworkNode> node = nodeAdapter.getNode(offsetPos);
            if (node.isPresent()) {
                return node;
            }
        }

        return Optional.empty();
    }

    private Set<Network> getNeighboringNetworks(NetworkNodeAdapter nodeAdapter, BlockPos pos) {
        Set<Network> neighboringNetworks = new HashSet<>();
        for (Direction dir : Direction.values()) {
            BlockPos offsetPos = pos.offset(dir);

            nodeAdapter.getNode(offsetPos).ifPresent(node -> {
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

    private void addNetwork(Network network) {
        LOGGER.debug("Network {} has been added", network.getId());
        networks.put(network.getId(), network);
    }

    @Override
    public Collection<Network> getNetworks() {
        return networks.values();
    }
}
