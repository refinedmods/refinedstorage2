package com.refinedmods.refinedstorage2.platform.fabric.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.CableNetworkNode;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CableBlockEntity extends InternalNetworkNodeContainerBlockEntity<CableNetworkNode> {
    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(Rs2Mod.BLOCK_ENTITIES.getCable(), pos, state, new CableNetworkNode(Rs2Config.get().getCable().getEnergyUsage()));
    }
}
