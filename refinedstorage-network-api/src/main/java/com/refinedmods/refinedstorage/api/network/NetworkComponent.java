package com.refinedmods.refinedstorage.api.network;

import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;

import java.util.Set;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.1")
public interface NetworkComponent {
    default void onContainerAdded(final NetworkNodeContainer container) {
    }

    default void onContainerRemoved(final NetworkNodeContainer container) {
    }

    default void onNetworkRemoved() {
    }

    default void onNetworkSplit(final Set<Network> networks) {
    }

    default void onNetworkMergedWith(final Network newMainNetwork) {
    }
}
