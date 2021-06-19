package com.refinedmods.refinedstorage2.core.network.component;

import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHost;

import java.util.Set;

public interface NetworkComponent {
    void onHostAdded(NetworkNodeHost<?> host);

    void onHostRemoved(NetworkNodeHost<?> host);

    void onNetworkRemoved();

    void onNetworkSplit(Set<Network> networks);
}
