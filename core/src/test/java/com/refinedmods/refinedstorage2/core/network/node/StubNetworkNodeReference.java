package com.refinedmods.refinedstorage2.core.network.node;

import java.util.Objects;
import java.util.Optional;

public class StubNetworkNodeReference implements NetworkNodeReference {
    private final NetworkNode node;

    public StubNetworkNodeReference(NetworkNode node) {
        this.node = node;
    }

    @Override
    public Optional<NetworkNode> get() {
        return Optional.ofNullable(node);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubNetworkNodeReference that = (StubNetworkNodeReference) o;
        return node.equals(that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node);
    }

    @Override
    public String toString() {
        return "StubNetworkNodeReference{" +
                "node=" + node +
                '}';
    }
}
