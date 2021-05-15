package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeRepository;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.Collection;

public interface NetworkManager {
    Network onNodeAdded(NetworkNodeRepository nodeRepository, Position pos);

    void onNodeRemoved(NetworkNodeRepository nodeRepository, Position pos);

    Collection<Network> getNetworks();

    void update();
}
