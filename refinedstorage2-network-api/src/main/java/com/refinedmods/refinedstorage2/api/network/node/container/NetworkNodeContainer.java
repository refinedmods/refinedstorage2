package com.refinedmods.refinedstorage2.api.network.node.container;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
@FunctionalInterface
public interface NetworkNodeContainer {
    NetworkNode getNode();

    /**
     * If this container needs to be indexed by the network graph, so it can be queried quickly (by key),
     * you can return a key here.
     * The key must be kept stable, and must stay the same for the lifetime of the container.
     * If it changes after adding it into the graph, the container would not be removed from the key index when the
     * container is removed!
     * The container can be queried by
     * {@link com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent#getContainer(Object)}.
     *
     * @return the key, or null if indexing is not required
     */
    @Nullable
    default Object createKey() {
        return null;
    }

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
