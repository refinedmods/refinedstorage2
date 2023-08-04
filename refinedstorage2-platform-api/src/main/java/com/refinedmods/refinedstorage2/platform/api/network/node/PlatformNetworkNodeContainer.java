package com.refinedmods.refinedstorage2.platform.api.network.node;

import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
     * @param other     the state wanting to connect
     * @return whether the node can accept an incoming connection
     */
    boolean canAcceptIncomingConnection(Direction direction, BlockState other);

    BlockState getBlockState();

    @Nullable
    Level getLevel();

    BlockPos getBlockPos();
}
