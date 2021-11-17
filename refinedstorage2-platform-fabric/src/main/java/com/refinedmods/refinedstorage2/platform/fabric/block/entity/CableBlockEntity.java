package com.refinedmods.refinedstorage2.platform.fabric.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.CableNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class CableBlockEntity extends FabricNetworkNodeContainerBlockEntity<NetworkNodeImpl> {
    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(Rs2Mod.BLOCK_ENTITIES.getCable(), pos, state);
    }

    @Override
    protected NetworkNodeImpl createNode(BlockPos pos, CompoundTag tag) {
        return new CableNetworkNode(Rs2Config.get().getCable().getEnergyUsage());
    }
}
