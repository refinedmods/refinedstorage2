package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CableBlockEntity extends AbstractInternalNetworkNodeContainerBlockEntity<SimpleNetworkNode> {
    public CableBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getCable(),
            pos,
            state,
            new SimpleNetworkNode(Platform.INSTANCE.getConfig().getCable().getEnergyUsage())
        );
    }
}
