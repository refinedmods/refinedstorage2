package com.refinedmods.refinedstorage2.platform.fabric.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.relay.RelayNetworkNode;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RelayBlockEntity extends InternalNetworkNodeContainerBlockEntity<RelayNetworkNode> {
    public RelayBlockEntity(BlockPos pos, BlockState state) {
        super(Rs2Mod.BLOCK_ENTITIES.getRelay(), pos, state, new RelayNetworkNode());
    }
}
