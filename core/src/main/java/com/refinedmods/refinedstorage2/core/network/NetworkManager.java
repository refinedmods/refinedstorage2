package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;

import java.util.Collection;

public interface NetworkManager {
    Network onNodeAdded(NetworkNode node);

    void onNodeRemoved(NetworkNode node);

    Collection<Network> getNetworks();
}
