package com.refinedmods.refinedstorage2.core.network.component;

import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHost;

import java.util.Set;

public interface NetworkComponent {
    default void onHostAdded(NetworkNodeHost<?> host) {

    }

    default void onHostRemoved(NetworkNodeHost<?> host) {

    }

    default void onNetworkRemoved() {

    }

    default void onNetworkSplit(Set<Network> networks) {

    }

    default void onNetworkMerge(Network network) {

    }
}
