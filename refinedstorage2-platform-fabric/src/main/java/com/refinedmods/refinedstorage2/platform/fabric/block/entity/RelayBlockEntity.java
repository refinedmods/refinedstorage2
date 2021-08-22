package com.refinedmods.refinedstorage2.platform.fabric.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.relay.RelayNetworkNode;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class RelayBlockEntity extends NetworkNodeBlockEntity<RelayNetworkNode> {
    public RelayBlockEntity(BlockPos pos, BlockState state) {
        super(Rs2Mod.BLOCK_ENTITIES.getRelay(), pos, state);
    }

    @Override
    protected RelayNetworkNode createNode(BlockPos pos, NbtCompound tag) {
        return new RelayNetworkNode();
    }
}
