package com.refinedmods.refinedstorage2.core.network.node;

import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.Optional;

public class StubNetworkNodeReference  implements NetworkNodeReference {
    private final NetworkNode networkNode;
    private final BlockPos pos;

    public StubNetworkNodeReference(BlockPos pos, NetworkNode networkNode) {
        this.pos = pos;
        this.networkNode = networkNode;
    }

    @Override
    public Optional<NetworkNode> get() {
        return Optional.of(networkNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubNetworkNodeReference that = (StubNetworkNodeReference) o;
        return networkNode.equals(that.networkNode) && pos.equals(that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(networkNode, pos);
    }
}
