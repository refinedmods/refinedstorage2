package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.node.FabricNetworkNodeReference;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;

public abstract class NetworkNodeBlockEntity extends BlockEntity implements NetworkNode {
    protected Network network;

    public NetworkNodeBlockEntity(BlockEntityType<?> type) {
        super(type);
    }

    @Override
    public BlockPos getPosition() {
        return getPos();
    }

    @Override
    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public NetworkNodeReference createReference() {
        return new FabricNetworkNodeReference(world.getServer(), GlobalPos.create(world.getRegistryKey(), getPos()));
    }
}
