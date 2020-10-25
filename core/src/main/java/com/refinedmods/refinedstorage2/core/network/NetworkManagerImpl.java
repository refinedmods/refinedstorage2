package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.graph.GraphScanner;
import com.refinedmods.refinedstorage2.core.graph.GraphScannerResult;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeAdapter;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;
import java.util.stream.Collectors;

public class NetworkManagerImpl implements NetworkManager {
    private final NetworkNodeAdapter networkNodeAdapter;
    private final GraphScanner<NetworkNode, NetworkNodeRequest> graphScanner;
    private final Map<UUID, Network> networks = new HashMap<>();

    public NetworkManagerImpl(NetworkNodeAdapter networkNodeAdapter) {
        this.networkNodeAdapter = networkNodeAdapter;
        this.graphScanner = new GraphScanner<>(new NetworkNodeRequestHandler());
    }

    @Override
    public Network onNodeAdded(BlockPos pos) {
        if (!networkNodeAdapter.getNode(pos).isPresent()) {
            throw new NetworkManagerException(String.format("Could not find added node at position %s", pos));
        }

        Set<Network> neighboringNetworks = getNeighboringNetworks(pos);
        if (neighboringNetworks.isEmpty()) {
            return formNetwork(pos);
        } else {
            return mergeNetworks(neighboringNetworks, pos);
        }
    }

    private Network mergeNetworks(Set<Network> neighboringNetworks, BlockPos pos) {
        GraphScannerResult<NetworkNode> result = graphScanner.scanAt(new NetworkNodeRequest(networkNodeAdapter, pos));

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

    private Network formNetwork(BlockPos pos) {
        Network network = new NetworkImpl(UUID.randomUUID());
        addNetwork(network);

        GraphScannerResult<NetworkNode> result = graphScanner.scanAt(new NetworkNodeRequest(networkNodeAdapter, pos));

        result.getAllEntries().forEach(node -> {
            node.setNetwork(network);
            network.getNodeReferences().add(node.createReference());
        });

        return network;
    }

    @Override
    public void onNodeRemoved(NetworkNode node) {
        for (Network neighboringNetwork : getNeighboringNetworks(node.getPosition())) {
            if (neighboringNetwork != node.getNetwork()) {
                throw new NetworkManagerException(String.format("The network manager was left in invalid state. The network of a neighboring node doesn't match the origin node. The origin node is located at %s", node.getPosition()));
            }
        }

        Optional<NetworkNode> neighborNode = getFirstNeighboringNode(node.getPosition());
        if (neighborNode.isPresent()) {
            splitNetworks(neighborNode.get(), node.getPosition());
        } else {
            removeNetwork(node.getNetwork());
        }
    }

    private void splitNetworks(NetworkNode pivot, BlockPos removedPos) {
        Network pivotNetwork = pivot.getNetwork();
        Set<NetworkNode> pivotNodes = getNodesInNetwork(pivotNetwork);

        GraphScannerResult<NetworkNode> result = graphScanner.scanAt(new NetworkNodeRequest(networkNodeAdapter, pivot.getPosition()), pivotNodes);

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
                formNetwork(removedNode.getPosition());
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

    private Optional<NetworkNode> getFirstNeighboringNode(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos offsetPos = pos.offset(dir);

            Optional<NetworkNode> node = networkNodeAdapter.getNode(offsetPos);
            if (node.isPresent()) {
                return node;
            }
        }

        return Optional.empty();
    }

    private Set<Network> getNeighboringNetworks(BlockPos pos) {
        Set<Network> neighboringNetworks = new HashSet<>();
        for (Direction dir : Direction.values()) {
            BlockPos offsetPos = pos.offset(dir);

            networkNodeAdapter.getNode(offsetPos).ifPresent(node -> {
                Network network = node.getNetwork();
                if (network == null) {
                    throw new NetworkManagerException(String.format("The network manager was left in an invalid state. Network node at %s has no network!", offsetPos));
                }

                neighboringNetworks.add(network);
            });
        }

        return neighboringNetworks;
    }

    @Override
    public Optional<Network> getNetwork(UUID id) {
        return Optional.ofNullable(networks.get(id));
    }

    @Override
    public Collection<Network> getNetworks() {
        return networks.values();
    }

    private void removeNetwork(Network network) {
        networks.remove(network.getId());
    }

    private void addNetwork(Network network) {
        networks.put(network.getId(), network);
    }
}
