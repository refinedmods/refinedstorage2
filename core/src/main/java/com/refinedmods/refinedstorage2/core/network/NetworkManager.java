package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeAdapter;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public interface NetworkManager {
    Network onNodeAdded(NetworkNodeAdapter nodeAdapter, BlockPos pos);

    void onNodeRemoved(NetworkNodeAdapter nodeAdapter, BlockPos pos);

    Collection<Network> getNetworks();
}
