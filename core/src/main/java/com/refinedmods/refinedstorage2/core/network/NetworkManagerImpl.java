package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.graph.GraphScanner;
import com.refinedmods.refinedstorage2.core.graph.GraphScannerResult;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeAdapter;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.graph.NetworkNodeRequest;
import com.refinedmods.refinedstorage2.core.network.node.graph.NetworkNodeRequestHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;
import java.util.stream.Collectors;

public class NetworkManagerImpl implements NetworkManager {
    private final GraphScanner<NetworkNode, NetworkNodeRequest> graphScanner;
    private final Map<UUID, Network> networks = new HashMap<>();

    public NetworkManagerImpl() {
        this.graphScanner = new GraphScanner<>(new NetworkNodeRequestHandler());
    }

    @Override
    public Network onNodeAdded(NetworkNodeAdapter nodeAdapter, NetworkNode node) {
        BlockPos pos = node.getPosition();

        if (!nodeAdapter.getNode(pos).isPresent()) {
            throw new NetworkManagerException(String.format("Could not find added node at position %s", pos));
        }

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
            removeNetwork(it.next());
        }

        pivotNetwork.getNodeReferences().clear();
        result.getAllEntries().forEach(node -> {
            node.setNetwork(pivotNetwork);
            pivotNetwork.getNodeReferences().add(node.createReference());
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

        return network;
    }

    @Override
    public void onNodeRemoved(NetworkNodeAdapter nodeAdapter, NetworkNode node) {
        for (Network neighboringNetwork : getNeighboringNetworks(nodeAdapter, node.getPosition())) {
            if (neighboringNetwork != node.getNetwork()) {
                throw new NetworkManagerException(String.format("The network manager was left in invalid state. The network of a neighboring node doesn't match the origin node. The origin node is located at %s", node.getPosition()));
            }
        }

        Optional<NetworkNode> neighborNode = getFirstNeighboringNode(nodeAdapter, node.getPosition());
        if (neighborNode.isPresent()) {
            splitNetworks(nodeAdapter, neighborNode.get(), node.getPosition());
        } else {
            removeNetwork(node.getNetwork());
        }
    }

    private void splitNetworks(NetworkNodeAdapter nodeAdapter, NetworkNode pivot, BlockPos removedPos) {
        Network pivotNetwork = pivot.getNetwork();
        Set<NetworkNode> pivotNodes = getNodesInNetwork(pivotNetwork);

        GraphScannerResult<NetworkNode> result = graphScanner.scanAt(new NetworkNodeRequest(nodeAdapter, pivot.getPosition()), pivotNodes);

        pivotNetwork.getNodeReferences().clear();
        for (NetworkNode node : result.getAllEntries()) {
            pivotNetwork.getNodeReferences().add(node.createReference());
        }

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
                formNetwork(nodeAdapter, removedNode.getPosition());
            }
        }

        if (!foundRemovedNode) {
            throw new NetworkManagerException(String.format("The removed node at %s is still present in the world!", removedPos));
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
        networks.remove(network.getId());
    }

    private void addNetwork(Network network) {
        networks.put(network.getId(), network);
    }

    @Override
    public Collection<Network> getNetworks() {
        return networks.values();
    }
}
