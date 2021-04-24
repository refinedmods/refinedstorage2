package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.coreimpl.adapter.FabricWorldAdapter;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.node.FabricNetworkNodeReference;
import com.refinedmods.refinedstorage2.fabric.util.Positions;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CableBlockEntity extends NetworkNodeBlockEntity<NetworkNodeImpl> {
    public CableBlockEntity() {
        super(RefinedStorage2Mod.BLOCK_ENTITIES.getCable());
    }

    @Override
    protected NetworkNodeImpl createNode(World world, BlockPos pos) {
        return new NetworkNodeImpl(FabricWorldAdapter.of(world), Positions.ofBlockPos(pos), FabricNetworkNodeReference.of(world, pos));
    }
}
