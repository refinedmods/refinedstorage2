package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeRepository;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public interface NetworkManager {
    Network onNodeAdded(NetworkNodeRepository nodeRepository, BlockPos pos);

    void onNodeRemoved(NetworkNodeRepository nodeRepository, BlockPos pos);

    Collection<Network> getNetworks();
}
