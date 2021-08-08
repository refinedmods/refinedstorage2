package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NetworkNodeBlockEntityTicker<T extends NetworkNodeImpl> implements BlockEntityTicker<NetworkNodeBlockEntity<T>> {
    @Override
    public void tick(World world, BlockPos pos, BlockState state, NetworkNodeBlockEntity<T> blockEntity) {
        blockEntity.tick(world, pos, state);
    }
}
