package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.CableNetworkNode;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CableBlockEntity extends AbstractInternalNetworkNodeContainerBlockEntity<CableNetworkNode> {
    public CableBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getCable(),
            pos,
            state,
            new CableNetworkNode(Platform.INSTANCE.getConfig().getCable().getEnergyUsage())
        );
    }
}
