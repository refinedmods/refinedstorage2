package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class NetworkNodeBlockEntity<T extends NetworkNode> extends BlockEntity implements NetworkNode {
    protected T node;

    public NetworkNodeBlockEntity(BlockEntityType<?> type) {
        super(type);
    }

    @Override
    public void setLocation(World world, BlockPos pos) {
        super.setLocation(world, pos);

        node = createNode(world, pos);
    }

    protected abstract T createNode(World world, BlockPos pos);

    @Override
    public BlockPos getPosition() {
        return node.getPosition();
    }

    @Override
    public void setNetwork(Network network) {
        node.setNetwork(network);
    }

    @Override
    public Network getNetwork() {
        return node.getNetwork();
    }

    @Override
    public NetworkNodeReference createReference() {
        return node.createReference();
    }
}
