package com.refinedmods.refinedstorage2.platform.fabric.block;

import com.refinedmods.refinedstorage2.platform.fabric.block.entity.ticker.FabricNetworkNodeContainerBlockEntityTicker;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public abstract class NetworkNodeContainerBlock extends BaseBlock implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    protected NetworkNodeContainerBlock(Properties settings) {
        super(settings);

        if (hasActive()) {
            registerDefaultState(getStateDefinition().any()
                    .setValue(ACTIVE, false));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        if (hasActive()) {
            builder.add(ACTIVE);
        }
    }

    protected boolean hasActive() {
        return false;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return new FabricNetworkNodeContainerBlockEntityTicker();
    }
}
