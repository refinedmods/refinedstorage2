package com.refinedmods.refinedstorage2.fabric.block.entity.ticker;

import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.NetworkNodeBlockEntity;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.container.FabricNetworkNodeContainerRepository;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NetworkNodeBlockEntityTicker<T extends NetworkNodeBlockEntity<?>> implements BlockEntityTicker<T> {
    @Override
    public void tick(World world, BlockPos pos, BlockState state, T blockEntity) {
        if (world.isClient()) {
            return;
        }
        tick(world, state, blockEntity);
    }

    protected void tick(World world, BlockState state, T blockEntity) {
        blockEntity.initialize(new FabricNetworkNodeContainerRepository(world), Rs2Mod.API.getNetworkComponentRegistry());
        blockEntity.updateActiveness(state);
        blockEntity.getNode().update();
    }
}
