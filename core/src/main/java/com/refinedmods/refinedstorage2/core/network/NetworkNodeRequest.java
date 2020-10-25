package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeAdapter;
import net.minecraft.util.math.BlockPos;

public class NetworkNodeRequest {
    private final NetworkNodeAdapter networkNodeAdapter;
    private final BlockPos pos;

    public NetworkNodeRequest(NetworkNodeAdapter networkNodeAdapter, BlockPos pos) {
        this.networkNodeAdapter = networkNodeAdapter;
        this.pos = pos;
    }

    public NetworkNodeAdapter getNetworkNodeAdapter() {
        return networkNodeAdapter;
    }

    public BlockPos getPos() {
        return pos;
    }
}
