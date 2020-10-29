package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeAdapter;

import java.util.Collection;

public interface NetworkManager {
    Network onNodeAdded(NetworkNodeAdapter nodeAdapter, NetworkNode node);

    void onNodeRemoved(NetworkNodeAdapter nodeAdapter, NetworkNode node);

    Collection<Network> getNetworks();
}
