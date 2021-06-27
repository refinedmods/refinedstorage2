package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHostImpl;
import com.refinedmods.refinedstorage2.core.network.host.relay.RelayNetworkNodeHost;
import com.refinedmods.refinedstorage2.core.network.node.relay.RelayNetworkNode;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.coreimpl.adapter.FabricRs2WorldAdapter;
import com.refinedmods.refinedstorage2.fabric.util.Positions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RelayBlockEntity extends NetworkNodeBlockEntity<RelayNetworkNode> {
    public RelayBlockEntity() {
        super(Rs2Mod.BLOCK_ENTITIES.getRelay());
    }

    @Override
    protected NetworkNodeHostImpl<RelayNetworkNode> createHost(World world, BlockPos pos, RelayNetworkNode node) {
        return new RelayNetworkNodeHost(FabricRs2WorldAdapter.of(world), Positions.ofBlockPos(pos), node);
    }

    @Override
    protected RelayNetworkNode createNode(World world, BlockPos pos, CompoundTag tag) {
        return new RelayNetworkNode(FabricRs2WorldAdapter.of(world), Positions.ofBlockPos(pos));
    }
}
