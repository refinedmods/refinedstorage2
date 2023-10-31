package com.refinedmods.refinedstorage2.platform.common.support;

import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLevelInteractingNetworkNodeContainerBlockEntity<T extends AbstractNetworkNode>
    extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(
        AbstractLevelInteractingNetworkNodeContainerBlockEntity.class
    );

    protected AbstractLevelInteractingNetworkNodeContainerBlockEntity(
        final BlockEntityType<?> type,
        final BlockPos pos,
        final BlockState state,
        final T node
    ) {
        super(type, pos, state, node);
    }

    // used to handle rotations
    @Override
    public void setBlockState(final BlockState newBlockState) {
        super.setBlockState(newBlockState);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        initialize(serverLevel);
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        initialize(serverLevel);
    }

    protected final void initialize(final ServerLevel level) {
        final Direction direction = getDirection();
        if (direction == null) {
            LOGGER.warn(
                "Failed to initialize: could not extract direction from block at {}, state is {}",
                worldPosition,
                getBlockState()
            );
            return;
        }
        initialize(level, direction);
    }

    protected abstract void initialize(ServerLevel level, Direction direction);
}
