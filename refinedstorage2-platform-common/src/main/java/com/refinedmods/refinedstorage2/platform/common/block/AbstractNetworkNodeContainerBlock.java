package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractInternalNetworkNodeContainerBlockEntity;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class AbstractNetworkNodeContainerBlock extends AbstractBaseBlock implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    protected AbstractNetworkNodeContainerBlock(final Properties properties) {
        super(properties);

        if (hasActive()) {
            registerDefaultState(getStateDefinition().any().setValue(ACTIVE, false));
        }
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        if (hasActive()) {
            builder.add(ACTIVE);
        }
    }

    protected boolean hasActive() {
        return false;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        // TODO: Check block entity type.
        return (l, p, s, be) -> AbstractInternalNetworkNodeContainerBlockEntity.serverTick(
                s,
                (AbstractInternalNetworkNodeContainerBlockEntity<?>) be
        );
    }
}
