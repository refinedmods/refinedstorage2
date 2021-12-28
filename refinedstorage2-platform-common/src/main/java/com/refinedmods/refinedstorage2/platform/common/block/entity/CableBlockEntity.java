package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.CableNetworkNode;
import com.refinedmods.refinedstorage2.platform.abstractions.PlatformAbstractions;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CableBlockEntity extends InternalNetworkNodeContainerBlockEntity<CableNetworkNode> {
    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(
                BlockEntities.INSTANCE.getCable(),
                pos,
                state,
                new CableNetworkNode(PlatformAbstractions.INSTANCE.getConfig().getCable().getEnergyUsage())
        );
    }
}
