package com.refinedmods.refinedstorage2.api.network.node.container;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
@FunctionalInterface
public interface NetworkNodeContainer {
    NetworkNode getNode();

    /**
     * The container priority determines the order in which the remainder containers as the result of a network split
     * are re-initialized with a new network.
     * A container with the highest priority will be re-initialized first.
     *
     * @return the priority
     */
    default int getPriority() {
        return 0;
    }
}
