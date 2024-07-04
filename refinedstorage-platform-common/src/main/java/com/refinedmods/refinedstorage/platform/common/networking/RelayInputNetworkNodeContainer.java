package com.refinedmods.refinedstorage.platform.common.networking;

import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.platform.api.support.network.ConnectionSink;
import com.refinedmods.refinedstorage.platform.common.support.network.InWorldNetworkNodeContainerImpl;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

class RelayInputNetworkNodeContainer extends InWorldNetworkNodeContainerImpl {
    private final RelayBlockEntity blockEntity;

    RelayInputNetworkNodeContainer(final RelayBlockEntity blockEntity, final NetworkNode node) {
        super(blockEntity, node, "input", 0, blockEntity, null);
        this.blockEntity = blockEntity;
    }

    @Override
    public void addOutgoingConnections(final ConnectionSink sink) {
        final Direction direction = blockEntity.getDirectionInternal();
        for (final Direction otherDirection : Direction.values()) {
            if (otherDirection != direction || (blockEntity.isPassThrough() && blockEntity.isActiveInternal())) {
                sink.tryConnectInSameDimension(
                    blockEntity.getBlockPos().relative(otherDirection),
                    otherDirection.getOpposite()
                );
            }
        }
    }

    @Override
    public boolean canAcceptIncomingConnection(final Direction incomingDirection, final BlockState connectingState) {
        return incomingDirection != blockEntity.getDirectionInternal()
            || (blockEntity.isPassThrough() && blockEntity.isActiveInternal());
    }
}
