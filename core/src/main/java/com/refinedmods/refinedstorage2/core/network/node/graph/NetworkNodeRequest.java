package com.refinedmods.refinedstorage2.core.network.node.graph;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeRepository;
import net.minecraft.util.math.BlockPos;

public class NetworkNodeRequest {
    private final NetworkNodeRepository repository;
    private final BlockPos pos;

    public NetworkNodeRequest(NetworkNodeRepository repository, BlockPos pos) {
        this.repository = repository;
        this.pos = pos;
    }

    public NetworkNodeRepository getNetworkNodeRepository() {
        return repository;
    }

    public BlockPos getPos() {
        return pos;
    }
}
