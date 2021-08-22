package com.refinedmods.refinedstorage2.platform.fabric.block.entity.ticker;

import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.NetworkNodeBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.FabricConnectionProvider;

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
        blockEntity.getContainer().initialize(new FabricConnectionProvider(world), NetworkComponentRegistry.INSTANCE);
        blockEntity.updateActiveness(state);
        blockEntity.getContainer().update();
    }
}
