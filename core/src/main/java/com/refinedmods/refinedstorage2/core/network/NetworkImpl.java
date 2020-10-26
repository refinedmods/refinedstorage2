package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NetworkImpl implements Network {
    private final UUID id;
    private final Set<NetworkNodeReference> nodeReferences = new HashSet<>();

    public NetworkImpl(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Set<NetworkNodeReference> getNodeReferences() {
        return nodeReferences;
    }
}
