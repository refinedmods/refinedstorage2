package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public interface NetworkManager {
    Network onNodeAdded(BlockPos pos);

    void onNodeRemoved(NetworkNode node);

    Collection<Network> getNetworks();
}
