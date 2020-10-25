package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.graph.GraphScanner;
import com.refinedmods.refinedstorage2.core.graph.GraphScannerResult;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeAdapter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;

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
        NetworkNode node = networkNodeAdapter
                .getNode(pos)
                .orElseThrow(() -> new NetworkManagerException(String.format("Could not find added node at position %s", pos)));

        Set<Network> neighboringNetworks = getNeighboringNetworks(pos);
        if (neighboringNetworks.isEmpty()) {
            return formNetwork(node);
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

    private Network formNetwork(NetworkNode node) {
        Network network = new NetworkImpl(UUID.randomUUID(), node.createReference());
        addNetwork(network);

        node.setNetwork(network);

        return network;
    }

    @Override
    public void onNodeRemoved(BlockPos pos) {

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
