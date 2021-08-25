package com.refinedmods.refinedstorage2.platform.fabric.api.block;

import com.refinedmods.refinedstorage2.platform.fabric.api.block.entity.ticker.NetworkNodeContainerBlockEntityTicker;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class NetworkNodeContainerBlock extends Block implements BlockEntityProvider {
    public NetworkNodeContainerBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (BlockEntityTicker<T>) new NetworkNodeContainerBlockEntityTicker<>();
    }
}
