package com.refinedmods.refinedstorage2.platform.api.support.network;

import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.1")
public interface PlatformNetworkNodeContainer extends NetworkNodeContainer {
    /**
     * Called when a node is about to be added into the network graph.
     * Add outgoing connections here.
     * If the outgoing connections ever change, call {@link
     * com.refinedmods.refinedstorage2.platform.api.PlatformApi#requestNetworkNodeUpdate(NetworkNodeContainer, Level)}.
     *
     * @param sink the sink that accepts outgoing connections
     */
    void addOutgoingConnections(ConnectionSink sink);

    /**
     * Returns whether the (not yet discovered) node can accept an incoming connection.
     * Changes to this return value won't cause a rebuild of the network state.
     * If the return value ever changes, call {@link
     * com.refinedmods.refinedstorage2.platform.api.PlatformApi#requestNetworkNodeUpdate(NetworkNodeContainer, Level)}.
     *
     * @param incomingDirection the incoming direction
     * @param connectingState   the state wanting to connect
     * @return whether the node can accept an incoming connection
     */
    boolean canAcceptIncomingConnection(Direction incomingDirection, BlockState connectingState);

    BlockState getContainerBlockState();

    GlobalPos getContainerPosition();
}
