package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
