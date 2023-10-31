package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.impl.node.SimpleNetworkNode;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// TODO: fix inheritance (no need for RedstoneMode here!)
public final class SimpleNetworkNodeContainerBlockEntity
    extends AbstractInternalNetworkNodeContainerBlockEntity<SimpleNetworkNode> {
    public SimpleNetworkNodeContainerBlockEntity(final BlockEntityType<SimpleNetworkNodeContainerBlockEntity> type,
                                                 final BlockPos pos,
                                                 final BlockState state,
                                                 final long energyUsage) {
        super(type, pos, state, new SimpleNetworkNode(energyUsage));
    }
}
