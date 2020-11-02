package com.refinedmods.refinedstorage2.fabric.block;

import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.node.FabricNetworkNodeAdapter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class NetworkNodeBlock extends Block implements BlockEntityProvider {
    public NetworkNodeBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);

        if (world instanceof ServerWorld) {
            RefinedStorage2Mod.API.getNetworkManager(world.getServer()).onNodeAdded(new FabricNetworkNodeAdapter(world), pos);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world instanceof ServerWorld) {
                RefinedStorage2Mod.API.getNetworkManager(world.getServer()).onNodeRemoved(new FabricNetworkNodeAdapter(world), pos);
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }
}
