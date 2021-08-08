package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainerImpl;
import com.refinedmods.refinedstorage2.core.network.node.container.relay.RelayNetworkNodeContainer;
import com.refinedmods.refinedstorage2.core.network.node.relay.RelayNetworkNode;
import com.refinedmods.refinedstorage2.core.util.Direction;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.coreimpl.adapter.FabricRs2WorldAdapter;
import com.refinedmods.refinedstorage2.fabric.util.Positions;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RelayBlockEntity extends NetworkNodeBlockEntity<RelayNetworkNode> {
    public RelayBlockEntity(BlockPos pos, BlockState state) {
        super(Rs2Mod.BLOCK_ENTITIES.getRelay(), pos, state);
    }

    @Override
    protected NetworkNodeContainerImpl<RelayNetworkNode> createContainer(World world, BlockPos pos, RelayNetworkNode node) {
        return new RelayNetworkNodeContainer(FabricRs2WorldAdapter.of(world), Positions.ofBlockPos(pos), node);
    }

    @Override
    protected RelayNetworkNode createNode(World world, BlockPos pos, NbtCompound tag) {
        return new RelayNetworkNode(FabricRs2WorldAdapter.of(world), Positions.ofBlockPos(pos), Direction.NORTH);
    }
}
