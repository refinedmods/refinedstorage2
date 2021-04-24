package com.refinedmods.refinedstorage2.core.network.node;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.refinedmods.refinedstorage2.core.util.Position;

public class FakeNetworkNodeRepository implements NetworkNodeRepository {
    private final Map<Position, NetworkNode> nodes = new HashMap<>();

    public NetworkNode setNode(Position pos) {
        NetworkNode node = new FakeNetworkNode(pos);
        nodes.put(pos, node);
        return node;
    }

    public void removeNode(Position pos) {
        nodes.remove(pos);
    }

    @Override
    public Optional<NetworkNode> getNode(Position pos) {
        return Optional.ofNullable(nodes.get(pos));
    }
}
