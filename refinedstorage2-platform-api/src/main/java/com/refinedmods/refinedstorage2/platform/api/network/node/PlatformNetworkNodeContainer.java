package com.refinedmods.refinedstorage2.platform.api.network.node;

import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import net.minecraft.core.Direction;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.1")
public interface PlatformNetworkNodeContainer extends NetworkNodeContainer {
    /**
     * Returns whether the already discovered node can perform an outgoing connection.
     *
     * @param direction the outgoing direction
     * @return whether the node can perform an outgoing connection
     */
    boolean canPerformOutgoingConnection(Direction direction);

    /**
     * Returns whether the not yet discovered node can accept an incoming connection.
     *
     * @param direction the incoming direction
     * @return whether the node can accept an incoming connection
     */
    boolean canAcceptIncomingConnection(Direction direction);
}
