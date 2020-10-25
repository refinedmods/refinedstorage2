package com.refinedmods.refinedstorage2.core.network.node;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeNetworkNodeAdapter implements NetworkNodeAdapter {
    private final Map<BlockPos, NetworkNode> nodes = new HashMap<>();

    public NetworkNode setNode(BlockPos pos) {
        NetworkNode node = new FakeNetworkNode(pos);
        nodes.put(pos, node);
        return node;
    }

    public void removeNode(BlockPos pos) {
        nodes.remove(pos);
    }

    @Override
    public Optional<NetworkNode> getNode(BlockPos pos) {
        return Optional.ofNullable(nodes.get(pos));
    }
}
