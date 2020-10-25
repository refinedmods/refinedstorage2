package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.adapter.WorldAdapter;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeAdapter;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;

public class NetworkManagerImpl implements NetworkManager {
    private final NetworkNodeAdapter networkNodeAdapter;
    private final Map<UUID, Network> networks = new HashMap<>();

    public NetworkManagerImpl(NetworkNodeAdapter networkNodeAdapter) {
        this.networkNodeAdapter = networkNodeAdapter;
    }

    @Override
    public Network onNodeAdded(WorldAdapter worldAdapter, BlockPos pos) {
        NetworkNodeReference nodeRef = networkNodeAdapter.getReference(pos);

        return formNetwork(nodeRef, pos);
    }

    @Override
    public void onNodeRemoved(BlockPos pos) {

    }

    private Network formNetwork(NetworkNodeReference initialNodeRef, BlockPos pos) {
        UUID id = UUID.randomUUID();
        Network network = new NetworkImpl(id);

        network.getNodeReferences().add(initialNodeRef);

        networks.put(id, network);
        return network;
    }

    private Set<Network> getNeighboringNetworks(BlockPos pos) {
        Set<Network> networks = new HashSet<>();
        for (Direction dir : Direction.values()) {
            BlockPos offset = pos.offset(dir);

            NetworkNodeReference nodeRef = networkNodeAdapter.getReference(offset);
            nodeRef.get().ifPresent(node -> networks.add(node.getNetwork()));
        }

        return networks;
    }

    @Override
    public Optional<Network> getNetwork(UUID id) {
        return Optional.ofNullable(networks.get(id));
    }
}
