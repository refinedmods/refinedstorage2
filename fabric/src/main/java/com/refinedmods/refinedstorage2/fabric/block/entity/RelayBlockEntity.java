package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.api.core.Direction;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainerImpl;
import com.refinedmods.refinedstorage2.api.network.node.container.relay.RelayNetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.network.node.relay.RelayNetworkNode;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.util.Positions;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class RelayBlockEntity extends NetworkNodeBlockEntity<RelayNetworkNode> {
    public RelayBlockEntity(BlockPos pos, BlockState state) {
        super(Rs2Mod.BLOCK_ENTITIES.getRelay(), pos, state);
    }

    @Override
    protected NetworkNodeContainerImpl<RelayNetworkNode> createContainer(BlockPos pos, RelayNetworkNode node) {
        return new RelayNetworkNodeContainer(Positions.ofBlockPos(pos), node);
    }

    @Override
    protected RelayNetworkNode createNode(BlockPos pos, NbtCompound tag) {
        return new RelayNetworkNode(Positions.ofBlockPos(pos), Direction.NORTH);
    }
}
