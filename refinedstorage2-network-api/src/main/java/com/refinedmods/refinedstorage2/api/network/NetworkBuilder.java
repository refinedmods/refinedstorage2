package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface NetworkBuilder {
    /**
     * Initialized a not yet connected network node container.
     * If the network node already has an associated {@link Network}, calling this will do nothing.
     * This will perform a merge operation.
     *
     * @param container          the container
     * @param connectionProvider the connection provider
     * @return true if the container has no network yet, and the initialization succeeded, false otherwise
     */
    boolean initialize(NetworkNodeContainer container, ConnectionProvider connectionProvider);

    /**
     * Removes a container from its network.
     * Will remove the network if the container is the last container in the network, or otherwise it performs
     * a split operation.
     *
     * @param container          the container
     * @param connectionProvider the connection provider
     */
    void remove(NetworkNodeContainer container, ConnectionProvider connectionProvider);

    /**
     * Updates the network associated with the given container. Makes the network state adapt to connection changes
     * of an already connected container.
     * Will perform split and/or merge operations.
     *
     * @param container          the container
     * @param connectionProvider the connection provider
     */
    void update(NetworkNodeContainer container, ConnectionProvider connectionProvider);
}
