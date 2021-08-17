package com.refinedmods.refinedstorage2.fabric.block;

import com.refinedmods.refinedstorage2.core.Rs2CoreApiFacade;
import com.refinedmods.refinedstorage2.fabric.api.container.FabricNetworkNodeContainerRepository;
import com.refinedmods.refinedstorage2.fabric.block.entity.NetworkNodeBlockEntity;
import com.refinedmods.refinedstorage2.fabric.block.entity.ticker.NetworkNodeBlockEntityTicker;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class NetworkNodeBlock extends BaseBlock implements BlockEntityProvider {
    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

    protected NetworkNodeBlock(Settings settings) {
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
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            super.onStateReplaced(state, world, pos, newState, moved);

            if (world instanceof ServerWorld && blockEntity instanceof NetworkNodeBlockEntity) {
                ((NetworkNodeBlockEntity) blockEntity).remove(new FabricNetworkNodeContainerRepository(world), Rs2CoreApiFacade.INSTANCE.getNetworkComponentRegistry());
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return new NetworkNodeBlockEntityTicker();
    }
}
