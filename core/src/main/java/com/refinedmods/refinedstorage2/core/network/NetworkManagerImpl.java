package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.graph.GraphScanner;
import com.refinedmods.refinedstorage2.core.graph.GraphScannerResult;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeAdapter;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import net.minecraft.util.Pair;
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
        Network mainNetwork = it.next();
        while (it.hasNext()) {
            removeNetwork(it.next());
        }

        mainNetwork.getNodeReferences().clear();

        result.getAllEntries().forEach(node -> {
            node.setNetwork(mainNetwork);
            mainNetwork.getNodeReferences().add(node.createReference());
        });

        return mainNetwork;
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
    public Set<Network> onNodeRemoved(BlockPos pos) {
        // TODO verify whether node is removed.

        Set<Network> neighboringNetworks = getNeighboringNetworks(pos);
        // TODO verify networks are the  same.

        Optional<Pair<Network, BlockPos>> firstNeighboringNetwork = getFirstNeighboringNetwork(pos);
        if (firstNeighboringNetwork.isPresent()) {
            Network neighborNetwork = firstNeighboringNetwork.get().getLeft();
            BlockPos neighborPos = firstNeighboringNetwork.get().getRight();
            Set<NetworkNode> neighborNetworkNodes = neighborNetwork.getNodeReferences()
                    .stream()
                    .map(NetworkNodeReference::get)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());

            GraphScannerResult<NetworkNode> result = graphScanner.scanAt(new NetworkNodeRequest(networkNodeAdapter, neighborPos), neighborNetworkNodes);

            neighborNetwork.getNodeReferences().clear();
            for (NetworkNode remainingNode : result.getAllEntries()) {
                neighborNetwork.getNodeReferences().add(remainingNode.createReference());
            }

            for (NetworkNode removedNode : result.getRemovedEntries()) {
                removedNode.setNetwork(null);
            }

            for (NetworkNode removedNode : result.getRemovedEntries()) {
                if (!removedNode.getPosition().equals(pos) && removedNode.getNetwork() == null) {
                    formNetwork(removedNode.getPosition());
                }
            }
        } else {
            // TODO remove network..
        }

        return Collections.emptySet();
    }

    private Optional<Pair<Network, BlockPos>> getFirstNeighboringNetwork(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos offsetPos = pos.offset(dir);

            Optional<NetworkNode> node = networkNodeAdapter.getNode(offsetPos);
            if (node.isPresent()) {
                return node.map(n -> {
                    // TODO check n.getNetwork()
                    return new Pair<>(n.getNetwork(), offsetPos);
                });
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
