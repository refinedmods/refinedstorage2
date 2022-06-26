package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.Set;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.1")
public interface NetworkComponent {
    default void onContainerAdded(NetworkNodeContainer container) {

    }

    default void onContainerRemoved(NetworkNodeContainer container) {

    }

    default void onNetworkRemoved() {

    }

    default void onNetworkSplit(Set<Network> networks) {

    }

    default void onNetworkMergedWith(Network network) {

    }
}
