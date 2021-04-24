package com.refinedmods.refinedstorage2.core.network.node.graph;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeRepository;
import com.refinedmods.refinedstorage2.core.util.Position;

public class NetworkNodeRequest {
    private final NetworkNodeRepository repository;
    private final Position pos;

    public NetworkNodeRequest(NetworkNodeRepository repository, Position pos) {
        this.repository = repository;
        this.pos = pos;
    }

    public NetworkNodeRepository getNetworkNodeRepository() {
        return repository;
    }

    public Position getPos() {
        return pos;
    }
}
