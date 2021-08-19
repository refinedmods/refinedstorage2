package com.refinedmods.refinedstorage2.fabric.block.entity.ticker;

import com.refinedmods.refinedstorage2.core.Rs2CoreApiFacade;
import com.refinedmods.refinedstorage2.fabric.api.container.FabricNetworkNodeContainerRepository;
import com.refinedmods.refinedstorage2.fabric.block.entity.NetworkNodeBlockEntity;

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
        // TODO: Remove this allocation
        blockEntity.initialize(new FabricNetworkNodeContainerRepository(world), Rs2CoreApiFacade.INSTANCE.getNetworkComponentRegistry());
        blockEntity.updateActiveness(state);
        blockEntity.getNode().update();
    }
}
