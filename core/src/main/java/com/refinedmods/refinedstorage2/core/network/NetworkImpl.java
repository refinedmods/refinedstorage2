package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class NetworkImpl implements Network {
    private final UUID id;
    private final Set<NetworkNodeReference> nodeReferences = new HashSet<>();

    public NetworkImpl(UUID id, NetworkNodeReference initialNodeReference) {
        this.id = id;
        this.nodeReferences.add(initialNodeReference);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Set<NetworkNodeReference> getNodeReferences() {
        return nodeReferences;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkImpl network = (NetworkImpl) o;
        return Objects.equals(id, network.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
