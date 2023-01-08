package com.refinedmods.refinedstorage2.platform.api.network.node;

import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.1")
public interface PlatformNetworkNodeContainer extends NetworkNodeContainer {
    /**
     * Returns whether the already discovered node can perform an outgoing connection.
     * Changes to this return value won't cause a rebuild of the network state.
     * If the return value ever changes, call {@link
     * com.refinedmods.refinedstorage2.platform.api.PlatformApi#requestNetworkNodeUpdate(NetworkNodeContainer, Level)}.
     *
     * @param direction the outgoing direction
     * @return whether the node can perform an outgoing connection
     */
    boolean canPerformOutgoingConnection(Direction direction);

    /**
     * Returns whether the not yet discovered node can accept an incoming connection.
     * Changes to this return value won't cause a rebuild of the network state.
     * If the return value ever changes, call {@link
     * com.refinedmods.refinedstorage2.platform.api.PlatformApi#requestNetworkNodeUpdate(NetworkNodeContainer, Level)}.
     *
     * @param direction the incoming direction
     * @return whether the node can accept an incoming connection
     */
    boolean canAcceptIncomingConnection(Direction direction);

    /**
     * returns whether the node can connect to another node. This is useful if you want a node only to connect to a
     * special node, or disallow connection to i.e. other colored nodes.
     * If the return value ever changes, call {@link
     * com.refinedmods.refinedstorage2.platform.api.PlatformApi#requestNetworkNodeUpdate(NetworkNodeContainer, Level)}.
     *
     * @param other the neighboring node
     * @return whether the node is allowed to connect to the neighbor
     */
    default boolean canConnectTo(PlatformNetworkNodeContainer other) {
        return true;
    }
}
