package com.refinedmods.refinedstorage.platform.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayOutputNetworkNode;
import com.refinedmods.refinedstorage.platform.api.support.network.ConnectionSink;
import com.refinedmods.refinedstorage.platform.common.support.network.InWorldNetworkNodeContainerImpl;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

class RelayOutputNetworkNodeContainer extends InWorldNetworkNodeContainerImpl {
    private final RelayBlockEntity blockEntity;

    RelayOutputNetworkNodeContainer(final RelayBlockEntity blockEntity, final RelayOutputNetworkNode node) {
        super(blockEntity, node, "output", 0, blockEntity, null);
        this.blockEntity = blockEntity;
    }

    // The output network node container must always have an outgoing and incoming connection.
    // If not, network node containers after the output network node container may end up without a network
    // because the graph algorithm won't be able to work properly and won't be able to reassign a new network.
    // The output network node container *never* provides a connection with the input network node container.
    // The input network node container controls whether the output network node container is connected.
    @Override
    public void addOutgoingConnections(final ConnectionSink sink) {
        final Direction direction = blockEntity.getDirectionInternal();
        sink.tryConnectInSameDimension(
            blockEntity.getBlockPos().relative(direction),
            direction.getOpposite()
        );
    }

    @Override
    public boolean canAcceptIncomingConnection(final Direction incomingDirection, final BlockState connectingState) {
        return incomingDirection == blockEntity.getDirectionInternal();
    }
}
