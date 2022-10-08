package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class InterfaceBlockEntity extends AbstractInternalNetworkNodeContainerBlockEntity<SimpleNetworkNode> {
    public InterfaceBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getInterface(),
            pos,
            state,
            new SimpleNetworkNode(Platform.INSTANCE.getConfig().getInterface().getEnergyUsage())
        );
    }
}
