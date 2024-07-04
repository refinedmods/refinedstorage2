package com.refinedmods.refinedstorage.platform.api.support.network;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.6")
public interface ConnectionLogic {
    /**
     * Add outgoing connections here.
     * Called when a node is about to be added into the network graph.
     * If the outgoing connections ever change, call {@link
     * com.refinedmods.refinedstorage.platform.api.PlatformApi#onNetworkNodeContainerUpdated(
     *InWorldNetworkNodeContainer, Level)}.
     *
     * @param sink the sink that accepts outgoing connections
     */
    void addOutgoingConnections(ConnectionSink sink);

    /**
     * Returns whether the (not yet discovered) node can accept an incoming connection.
     * Changes to this return value won't cause a rebuild of the network state.
     * If the return value ever changes, call {@link
     * com.refinedmods.refinedstorage.platform.api.PlatformApi#onNetworkNodeContainerUpdated(
     *InWorldNetworkNodeContainer, Level)}.
     *
     * @param incomingDirection the incoming direction
     * @param connectingState   the state wanting to connect
     * @return whether the node can accept an incoming connection
     */
    boolean canAcceptIncomingConnection(Direction incomingDirection, BlockState connectingState);
}
