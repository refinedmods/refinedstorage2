package com.refinedmods.refinedstorage2.core.network.node;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class FakeNetworkNodeAdapter implements NetworkNodeAdapter {
    private final Map<BlockPos, NetworkNodeReference> refs = new HashMap<>();

    public NetworkNode setNode(BlockPos pos, NetworkNode node) {
        refs.put(pos, new StubNetworkNodeReference(pos, node));
        return node;
    }

    @Override
    public NetworkNodeReference getReference(BlockPos pos) {
        return refs.getOrDefault(pos, NullNetworkNodeReference.INSTANCE);
    }
}
