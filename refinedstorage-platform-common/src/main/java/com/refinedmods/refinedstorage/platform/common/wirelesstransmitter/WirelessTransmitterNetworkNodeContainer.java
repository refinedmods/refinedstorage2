package com.refinedmods.refinedstorage.platform.common.wirelesstransmitter;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.platform.api.support.network.ConnectionLogic;
import com.refinedmods.refinedstorage.platform.api.wirelesstransmitter.WirelessTransmitter;
import com.refinedmods.refinedstorage.platform.common.support.network.InWorldNetworkNodeContainerImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

class WirelessTransmitterNetworkNodeContainer extends InWorldNetworkNodeContainerImpl
    implements WirelessTransmitter {
    private final WirelessTransmitterBlockEntity blockEntity;
    private final AbstractNetworkNode node;

    WirelessTransmitterNetworkNodeContainer(final WirelessTransmitterBlockEntity blockEntity,
                                            final AbstractNetworkNode node,
                                            final String name,
                                            final ConnectionLogic connectionLogic) {
        super(blockEntity, node, name, 0, connectionLogic, null);
        this.blockEntity = blockEntity;
        this.node = node;
    }

    @Override
    public boolean isInRange(final ResourceKey<Level> dimension, final Vec3 position) {
        final Level level = blockEntity.getLevel();
        if (level == null || level.dimension() != dimension) {
            return false;
        }
        if (!node.isActive()) {
            return false;
        }
        final BlockPos pos = blockEntity.getBlockPos();
        final double distance = Math.sqrt(
            Math.pow(pos.getX() - position.x(), 2)
                + Math.pow(pos.getY() - position.y(), 2)
                + Math.pow(pos.getZ() - position.z(), 2)
        );
        return distance <= blockEntity.getRange();
    }
}
