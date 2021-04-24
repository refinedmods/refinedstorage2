package com.refinedmods.refinedstorage2.fabric.block;

import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.node.FabricNetworkNodeRepository;
import com.refinedmods.refinedstorage2.fabric.util.Positions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class NetworkNodeBlock extends BaseBlock implements BlockEntityProvider {
    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

    public NetworkNodeBlock(Settings settings) {
        super(settings);

        if (hasActive()) {
            setDefaultState(getStateManager().getDefaultState()
                    .with(ACTIVE, false));
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);

        if (hasActive()) {
            builder.add(ACTIVE);
        }
    }

    protected boolean hasActive() {
        return false;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);

        if (world instanceof ServerWorld) {
            Rs2Mod.API.getNetworkManager(world.getServer()).onNodeAdded(new FabricNetworkNodeRepository(world), Positions.ofBlockPos(pos));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world instanceof ServerWorld) {
                Rs2Mod.API.getNetworkManager(world.getServer()).onNodeRemoved(new FabricNetworkNodeRepository(world), Positions.ofBlockPos(pos));
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }
}
